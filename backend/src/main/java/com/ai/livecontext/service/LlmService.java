package com.ai.livecontext.service;

import com.ai.livecontext.domain.ChatResponse;
import com.ai.livecontext.util.CorrelationIdHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class LlmService {

  private static final Logger logger = LoggerFactory.getLogger(LlmService.class);
  private static final int MAX_TOOL_ITERATIONS = 5;

  private final WebClient webClient;
  private final ObjectMapper objectMapper;
  private final McpClientService mcpClientService;

  @Value("${livecontext.ollama.base-url}")
  private String ollamaBaseUrl;

  @Value("${livecontext.ollama.model}")
  private String ollamaModel;

  @Value("${livecontext.ollama.mock-mode:false}")
  private boolean mockMode;

  public LlmService(McpClientService mcpClientService, ObjectMapper objectMapper) {
    this.mcpClientService = mcpClientService;
    this.objectMapper = objectMapper;
    this.webClient = WebClient.builder().build();
  }

  public Mono<ChatResponse> chat(String question) {
    long startTime = System.currentTimeMillis();
    String correlationId = CorrelationIdHolder.get();

    logger.info(
        "[llm_request_start] Starting LLM chat request | model={} questionLength={} mockMode={} correlationId={}",
        ollamaModel,
        question.length(),
        mockMode,
        correlationId);

    if (mockMode) {
      return generateMockResponse(question);
    }

    // Initialize messages array with user message
    ArrayNode messages = objectMapper.createArrayNode();
    ObjectNode userMessage = objectMapper.createObjectNode();
    userMessage.put("role", "user");
    userMessage.put("content", question);
    messages.add(userMessage);

    // Create evidence collector
    List<ChatResponse.Evidence> evidence = new ArrayList<>();

    // Start the agentic loop
    return executeAgenticLoop(messages, evidence, 0, startTime, correlationId);
  }

  private Mono<ChatResponse> executeAgenticLoop(
      ArrayNode messages,
      List<ChatResponse.Evidence> evidence,
      int iteration,
      long startTime,
      String correlationId) {

    if (iteration >= MAX_TOOL_ITERATIONS) {
      logger.warn(
          "[llm_max_iterations] Reached maximum tool iterations | maxIterations={} correlationId={}",
          MAX_TOOL_ITERATIONS,
          correlationId);
      return extractFinalResponse(messages, evidence, startTime, correlationId);
    }

    // Build request with tools
    ObjectNode requestBody = objectMapper.createObjectNode();
    requestBody.put("model", ollamaModel);
    requestBody.set("messages", messages);
    requestBody.put("stream", false);
    requestBody.set("tools", buildToolDefinitions());

    String jsonBody;
    try {
      jsonBody = objectMapper.writeValueAsString(requestBody);
    } catch (Exception e) {
      return generateMockResponse("Error serializing request");
    }

    logger.debug(
        "[llm_chat_request] Sending chat request to LLM | url={} model={} iteration={} messageCount={} correlationId={}",
        ollamaBaseUrl + "/api/chat",
        ollamaModel,
        iteration,
        messages.size(),
        correlationId);

    return webClient
        .post()
        .uri(ollamaBaseUrl + "/api/chat")
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(jsonBody)
        .retrieve()
        .bodyToMono(String.class)
        .flatMap(
            responseBody -> {
              try {
                logger.debug(
                    "[llm_chat_response] Received chat response from LLM | responseLength={} correlationId={}",
                    responseBody.length(),
                    correlationId);

                JsonNode response = objectMapper.readTree(responseBody);
                JsonNode message = response.path("message");

                // Add assistant message to conversation
                messages.add(message);

                // Check for tool calls
                JsonNode toolCalls = message.path("tool_calls");
                if (toolCalls.isArray() && toolCalls.size() > 0) {
                  logger.info(
                      "[llm_tool_calls_detected] LLM requested tool calls | toolCount={} iteration={} correlationId={}",
                      toolCalls.size(),
                      iteration,
                      correlationId);

                  // Execute all tool calls and collect results
                  return executeToolCalls(toolCalls, evidence, correlationId)
                      .collectList()
                      .flatMap(
                          toolResults -> {
                            // Add tool results to messages
                            for (ObjectNode toolResult : toolResults) {
                              messages.add(toolResult);
                            }
                            // Continue the loop
                            return executeAgenticLoop(
                                messages, evidence, iteration + 1, startTime, correlationId);
                          });
                } else {
                  // No tool calls - extract final answer
                  String answer = message.path("content").asText("No response from LLM");
                  long duration = System.currentTimeMillis() - startTime;

                  logger.info(
                      "[llm_request_success] LLM request completed | model={} responseLength={} duration_ms={} iterations={} evidenceCount={} correlationId={}",
                      ollamaModel,
                      answer.length(),
                      duration,
                      iteration + 1,
                      evidence.size(),
                      correlationId);

                  return Mono.just(
                      ChatResponse.builder()
                          .answer(answer)
                          .evidence(evidence)
                          .correlationId(correlationId)
                          .build());
                }
              } catch (Exception e) {
                logger.error(
                    "[llm_parse_error] Failed to parse LLM response | error={} correlationId={}",
                    e.getMessage(),
                    correlationId);
                return Mono.error(e);
              }
            })
        .onErrorResume(
            error -> {
              long duration = System.currentTimeMillis() - startTime;
              logger.error(
                  "[llm_request_error] LLM request failed | error={} duration_ms={} correlationId={}",
                  error.getMessage(),
                  duration,
                  correlationId);
              return generateMockResponse(
                  "Error: " + error.getMessage()); // This converts Error to Success
            });
  }

  private Flux<ObjectNode> executeToolCalls(
      JsonNode toolCalls, List<ChatResponse.Evidence> evidence, String correlationId) {

    List<Mono<ObjectNode>> toolExecutions = new ArrayList<>();

    for (JsonNode toolCall : toolCalls) {
      JsonNode function = toolCall.path("function");
      String toolName = function.path("name").asText();
      JsonNode arguments = function.path("arguments");

      logger.info(
          "[llm_executing_tool] Executing tool requested by LLM | tool={} arguments={} correlationId={}",
          toolName,
          arguments.toString(),
          correlationId);

      Mono<ObjectNode> toolExecution =
          executeToolByName(toolName, arguments, correlationId)
              .map(
                  result -> {
                    // Create tool response message
                    ObjectNode toolMessage = objectMapper.createObjectNode();
                    toolMessage.put("role", "tool");
                    toolMessage.put("content", result.toString());

                    // Add evidence
                    evidence.add(
                        ChatResponse.Evidence.builder()
                            .type(toolName)
                            .source(mapToolToServer(toolName))
                            .timestamp(Instant.now().toString())
                            .summary(
                                "Tool result: "
                                    + (result.toString().length() > 100
                                        ? result.toString().substring(0, 100) + "..."
                                        : result.toString()))
                            .build());

                    return toolMessage;
                  })
              .onErrorResume(
                  e -> {
                    logger.error(
                        "[llm_tool_execution_error] Tool execution failed | tool={} error={} correlationId={}",
                        toolName,
                        e.getMessage(),
                        correlationId);

                    ObjectNode errorMessage = objectMapper.createObjectNode();
                    errorMessage.put("role", "tool");
                    errorMessage.put("content", "Error executing tool: " + e.getMessage());
                    return Mono.just(errorMessage);
                  });

      toolExecutions.add(toolExecution);
    }

    return Flux.merge(toolExecutions);
  }

  private Mono<JsonNode> executeToolByName(
      String toolName, JsonNode arguments, String correlationId) {
    ObjectNode params = objectMapper.createObjectNode();

    switch (toolName) {
      case "get_quote":
        params.put("symbol", arguments.path("symbol").asText("AAPL.US"));
        params.put("interval", arguments.path("interval").asText("daily"));
        return mcpClientService.callTool("market", "get_quote", params);

      case "search_news":
        params.put("query", arguments.path("query").asText(""));
        params.put("limit", arguments.path("limit").asInt(5));
        return mcpClientService.callTool("news", "search", params);

      case "get_weather":
        params.put("latitude", arguments.path("latitude").asDouble(40.7128));
        params.put("longitude", arguments.path("longitude").asDouble(-74.0060));
        return mcpClientService.callTool("weather", "get_forecast", params);

      default:
        return Mono.error(new IllegalArgumentException("Unknown tool: " + toolName));
    }
  }

  private String mapToolToServer(String toolName) {
    return switch (toolName) {
      case "get_quote" -> "Market MCP";
      case "search_news" -> "News MCP";
      case "get_weather" -> "Weather MCP";
      default -> "Unknown";
    };
  }

  private ArrayNode buildToolDefinitions() {
    ArrayNode tools = objectMapper.createArrayNode();

    // get_quote tool
    ObjectNode getQuoteTool = objectMapper.createObjectNode();
    getQuoteTool.put("type", "function");
    ObjectNode getQuoteFunction = objectMapper.createObjectNode();
    getQuoteFunction.put("name", "get_quote");
    getQuoteFunction.put(
        "description",
        "Get stock market quote and price data for a given stock symbol. Use this for any questions about stock prices, market data, or financial instruments.");
    ObjectNode getQuoteParams = objectMapper.createObjectNode();
    getQuoteParams.put("type", "object");
    ObjectNode getQuoteProperties = objectMapper.createObjectNode();
    ObjectNode symbolProp = objectMapper.createObjectNode();
    symbolProp.put("type", "string");
    symbolProp.put(
        "description",
        "Stock symbol with exchange suffix, e.g., AAPL.US for Apple, TSLA.US for Tesla, GOOGL.US for Google");
    getQuoteProperties.set("symbol", symbolProp);
    ObjectNode intervalProp = objectMapper.createObjectNode();
    intervalProp.put("type", "string");
    intervalProp.put("description", "Time interval: daily, weekly, or monthly");
    getQuoteProperties.set("interval", intervalProp);
    getQuoteParams.set("properties", getQuoteProperties);
    ArrayNode getQuoteRequired = objectMapper.createArrayNode();
    getQuoteRequired.add("symbol");
    getQuoteParams.set("required", getQuoteRequired);
    getQuoteFunction.set("parameters", getQuoteParams);
    getQuoteTool.set("function", getQuoteFunction);
    tools.add(getQuoteTool);

    // search_news tool
    ObjectNode searchNewsTool = objectMapper.createObjectNode();
    searchNewsTool.put("type", "function");
    ObjectNode searchNewsFunction = objectMapper.createObjectNode();
    searchNewsFunction.put("name", "search_news");
    searchNewsFunction.put(
        "description",
        "Search for recent news articles on a topic. Use this for questions about current events, news, or recent developments.");
    ObjectNode searchNewsParams = objectMapper.createObjectNode();
    searchNewsParams.put("type", "object");
    ObjectNode searchNewsProperties = objectMapper.createObjectNode();
    ObjectNode queryProp = objectMapper.createObjectNode();
    queryProp.put("type", "string");
    queryProp.put("description", "Search query for news articles");
    searchNewsProperties.set("query", queryProp);
    ObjectNode limitProp = objectMapper.createObjectNode();
    limitProp.put("type", "integer");
    limitProp.put("description", "Maximum number of results to return");
    searchNewsProperties.set("limit", limitProp);
    searchNewsParams.set("properties", searchNewsProperties);
    ArrayNode searchNewsRequired = objectMapper.createArrayNode();
    searchNewsRequired.add("query");
    searchNewsParams.set("required", searchNewsRequired);
    searchNewsFunction.set("parameters", searchNewsParams);
    searchNewsTool.set("function", searchNewsFunction);
    tools.add(searchNewsTool);

    // get_weather tool
    ObjectNode getWeatherTool = objectMapper.createObjectNode();
    getWeatherTool.put("type", "function");
    ObjectNode getWeatherFunction = objectMapper.createObjectNode();
    getWeatherFunction.put("name", "get_weather");
    getWeatherFunction.put(
        "description",
        "Get current weather and forecast for a location. Use this for questions about weather conditions.");
    ObjectNode getWeatherParams = objectMapper.createObjectNode();
    getWeatherParams.put("type", "object");
    ObjectNode getWeatherProperties = objectMapper.createObjectNode();
    ObjectNode latProp = objectMapper.createObjectNode();
    latProp.put("type", "number");
    latProp.put("description", "Latitude of the location");
    getWeatherProperties.set("latitude", latProp);
    ObjectNode lonProp = objectMapper.createObjectNode();
    lonProp.put("type", "number");
    lonProp.put("description", "Longitude of the location");
    getWeatherProperties.set("longitude", lonProp);
    getWeatherParams.set("properties", getWeatherProperties);
    ArrayNode getWeatherRequired = objectMapper.createArrayNode();
    getWeatherRequired.add("latitude");
    getWeatherRequired.add("longitude");
    getWeatherParams.set("required", getWeatherRequired);
    getWeatherFunction.set("parameters", getWeatherParams);
    getWeatherTool.set("function", getWeatherFunction);
    tools.add(getWeatherTool);

    return tools;
  }

  private Mono<ChatResponse> extractFinalResponse(
      ArrayNode messages,
      List<ChatResponse.Evidence> evidence,
      long startTime,
      String correlationId) {

    // Find the last assistant message
    String lastAnswer = "Unable to generate response";
    for (int i = messages.size() - 1; i >= 0; i--) {
      JsonNode msg = messages.get(i);
      if ("assistant".equals(msg.path("role").asText())) {
        lastAnswer = msg.path("content").asText(lastAnswer);
        break;
      }
    }

    long duration = System.currentTimeMillis() - startTime;
    logger.info(
        "[llm_request_success] LLM request completed (max iterations) | duration_ms={} evidenceCount={} correlationId={}",
        duration,
        evidence.size(),
        correlationId);

    return Mono.just(
        ChatResponse.builder()
            .answer(lastAnswer)
            .evidence(evidence)
            .correlationId(correlationId)
            .build());
  }

  private Mono<ChatResponse> generateMockResponse(String question) {
    String answer =
        "Mock response: I received your question about: \""
            + question
            + "\". "
            + "In a real scenario, I would use MCP tools to fetch market data, news, and weather"
            + " information to answer your question.";

    List<ChatResponse.Evidence> evidence = new ArrayList<>();
    evidence.add(
        ChatResponse.Evidence.builder()
            .type("market")
            .source("Mock Market Data")
            .timestamp("2026-01-01T00:00:00Z")
            .summary("Sample market quote data (mock)")
            .build());

    return Mono.just(
        ChatResponse.builder()
            .answer(answer)
            .evidence(evidence)
            .correlationId(CorrelationIdHolder.get())
            .build());
  }
}
