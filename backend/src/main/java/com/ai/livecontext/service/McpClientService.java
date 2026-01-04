package com.ai.livecontext.service;

import com.ai.livecontext.config.McpConfig;
import com.ai.livecontext.util.CorrelationIdHolder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class McpClientService {

  private static final Logger logger = LoggerFactory.getLogger(McpClientService.class);

  private final McpConfig mcpConfig;
  private final WebClient webClient;
  private final ObjectMapper objectMapper;

  public McpClientService(McpConfig mcpConfig, ObjectMapper objectMapper) {
    this.mcpConfig = mcpConfig;
    this.objectMapper = objectMapper;
    this.webClient = WebClient.builder().build();
  }

  public Mono<JsonNode> callTool(String server, String tool, JsonNode parameters) {
    long startTime = System.currentTimeMillis();
    String url = getServerUrl(server);
    String correlationId = CorrelationIdHolder.get();

    ObjectNode requestBody = objectMapper.createObjectNode();
    requestBody.put("tool", tool);
    requestBody.set("parameters", parameters);
    if (correlationId != null) {
      requestBody.put("correlationId", correlationId);
    }

    String jsonBody;
    try {
      jsonBody = objectMapper.writeValueAsString(requestBody);
    } catch (Exception e) {
      return Mono.error(e);
    }

    logger.info(
        "[mcp_tool_call_start] Calling MCP tool | server={} tool={} url={} body={} correlationId={}",
        server,
        tool,
        url,
        jsonBody,
        correlationId);

    return webClient
        .post()
        .uri(url + "/tools/" + tool)
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(jsonBody)
        .retrieve()
        .bodyToMono(String.class)
        .flatMap(
            responseBody -> {
              try {
                return Mono.just(objectMapper.readTree(responseBody));
              } catch (Exception e) {
                return Mono.error(e);
              }
            })
        .timeout(Duration.ofMillis(getServerTimeout(server)))
        .doOnSuccess(
            result -> {
              long duration = System.currentTimeMillis() - startTime;
              logger.info(
                  "[mcp_tool_call_success] MCP tool call succeeded | server={} tool={} duration_ms={} correlationId={}",
                  server,
                  tool,
                  duration,
                  correlationId);
            })
        .doOnError(
            error -> {
              long duration = System.currentTimeMillis() - startTime;
              logger.error(
                  "[mcp_tool_call_error] MCP tool call failed | server={} tool={} error={} duration_ms={} correlationId={}",
                  server,
                  tool,
                  error.getMessage(),
                  duration,
                  correlationId);
            });
  }

  private String getServerUrl(String server) {
    return switch (server.toLowerCase()) {
      case "market" -> mcpConfig.getMarket().getUrl();
      case "news" -> mcpConfig.getNews().getUrl();
      case "weather" -> mcpConfig.getWeather().getUrl();
      case "system" -> mcpConfig.getSystem().getUrl();
      default -> throw new IllegalArgumentException("Unknown MCP server: " + server);
    };
  }

  private int getServerTimeout(String server) {
    return switch (server.toLowerCase()) {
      case "market" -> mcpConfig.getMarket().getTimeout();
      case "news" -> mcpConfig.getNews().getTimeout();
      case "weather" -> mcpConfig.getWeather().getTimeout();
      case "system" -> mcpConfig.getSystem().getTimeout();
      default -> 10000;
    };
  }
}
