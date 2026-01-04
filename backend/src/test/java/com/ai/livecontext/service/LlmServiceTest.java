package com.ai.livecontext.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class LlmServiceTest {

  @Mock private McpClientService mcpClientService;

  private ObjectMapper objectMapper;
  private LlmService llmService;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    llmService = new LlmService(mcpClientService, objectMapper);

    // Set properties via reflection
    ReflectionTestUtils.setField(llmService, "ollamaBaseUrl", "http://localhost:11434");
    ReflectionTestUtils.setField(llmService, "ollamaModel", "qwen2.5:7b");
    ReflectionTestUtils.setField(llmService, "mockMode", true);
  }

  @Test
  void chat_shouldReturnMockResponseInMockMode() {
    String question = "What is the weather in NYC?";

    StepVerifier.create(llmService.chat(question))
        .expectNextMatches(
            response ->
                response.getAnswer() != null
                    && !response.getAnswer().isEmpty()
                    && response.getEvidence() != null)
        .verifyComplete();
  }

  @Test
  void chat_shouldHandleEmptyQuestion() {
    String question = "";

    StepVerifier.create(llmService.chat(question))
        .expectNextMatches(response -> response.getAnswer() != null)
        .verifyComplete();
  }

  @Test
  void chat_shouldReturnMockResponseWithEvidence() {
    String question = "Tell me about AAPL stock price";

    StepVerifier.create(llmService.chat(question))
        .expectNextMatches(
            response -> {
              // Mock mode should return evidence
              return response.getEvidence() != null;
            })
        .verifyComplete();
  }

  @Test
  void chat_shouldHandleLongQuestion() {
    String longQuestion = "What is ".repeat(100) + "the weather?";

    StepVerifier.create(llmService.chat(longQuestion))
        .expectNextMatches(response -> response.getAnswer() != null)
        .verifyComplete();
  }

  @Test
  void chat_shouldHandleMarketQuestion() {
    String question = "What is the stock price of AAPL?";

    StepVerifier.create(llmService.chat(question))
        .expectNextMatches(
            response -> response.getAnswer() != null && response.getEvidence() != null)
        .verifyComplete();
  }

  @Test
  void chat_shouldHandleNewsQuestion() {
    String question = "What are the latest news about technology?";

    StepVerifier.create(llmService.chat(question))
        .expectNextMatches(response -> response.getAnswer() != null)
        .verifyComplete();
  }

  @Test
  void chat_shouldHandleSpecialCharacters() {
    String question = "What's the price of $AAPL & GOOGL?";

    StepVerifier.create(llmService.chat(question))
        .expectNextMatches(response -> response.getAnswer() != null)
        .verifyComplete();
  }

  @Test
  void chat_shouldHandleNullCorrelationId() {
    // CorrelationIdHolder.set(null) should not cause issues
    com.ai.livecontext.util.CorrelationIdHolder.clear();
    String question = "Test question";

    StepVerifier.create(llmService.chat(question))
        .expectNextMatches(response -> response.getAnswer() != null)
        .verifyComplete();
  }

  /** New tests to cover real LLM logic (mockMode = false) */
  @Nested
  class AgenticLoopTests {
    private MockWebServer mockWebServer;

    @BeforeEach
    void setUp() throws IOException {
      mockWebServer = new MockWebServer();
      mockWebServer.start();

      // Re-initialize service with local mock server URL
      ReflectionTestUtils.setField(llmService, "ollamaBaseUrl", mockWebServer.url("/").toString());
      ReflectionTestUtils.setField(llmService, "mockMode", false);
    }

    @AfterEach
    void tearDown() throws IOException {
      mockWebServer.shutdown();
    }

    @Test
    void chat_shouldHandleToolCallsAndThenFinalResponse() throws InterruptedException {
      // 1. Mock LLM response with tool call
      String toolCallResponse =
          """
                {
                  "message": {
                    "role": "assistant",
                    "tool_calls": [
                      {
                        "function": {
                          "name": "get_quote",
                          "arguments": { "symbol": "AAPL.US" }
                        }
                      }
                    ]
                  }
                }
                """;

      // 2. Mock MCP Tool result
      ObjectNode toolResult = objectMapper.createObjectNode().put("price", 150.0);
      when(mcpClientService.callTool(eq("market"), eq("get_quote"), any()))
          .thenReturn(Mono.just(toolResult));

      // 3. Mock LLM final response
      String finalResponse =
          """
                {
                  "message": {
                    "role": "assistant",
                    "content": "The price of AAPL is 150.0"
                  }
                }
                """;

      mockWebServer.enqueue(
          new MockResponse()
              .setBody(toolCallResponse)
              .addHeader("Content-Type", "application/json"));
      mockWebServer.enqueue(
          new MockResponse().setBody(finalResponse).addHeader("Content-Type", "application/json"));

      StepVerifier.create(llmService.chat("Price of AAPL?"))
          .expectNextMatches(
              response ->
                  response.getAnswer().contains("150.0") && response.getEvidence().size() == 1)
          .verifyComplete();

      assertThat(mockWebServer.getRequestCount()).isEqualTo(2);
    }

    @Test
    void chat_shouldHandleUnknownToolError() {
      String toolCallResponse =
          """
                {
                  "message": {
                    "role": "assistant",
                    "tool_calls": [{ "function": { "name": "unknown_tool", "arguments": {} } }]
                  }
                }
                """;

      // The LLM will then get an error message in the next iteration
      String finalResponse =
          "{\"message\": {\"role\": \"assistant\", \"content\": \"Error encountered\"}}";

      mockWebServer.enqueue(new MockResponse().setBody(toolCallResponse));
      mockWebServer.enqueue(new MockResponse().setBody(finalResponse));

      StepVerifier.create(llmService.chat("Test?"))
          .expectNextMatches(response -> response.getAnswer().equals("Error encountered"))
          .verifyComplete();
    }

    @Test
    void chat_shouldHandleHttpErrorWithFallback() {
      mockWebServer.enqueue(new MockResponse().setResponseCode(500));

      StepVerifier.create(llmService.chat("Fail test"))
          .expectNextMatches(response -> response.getAnswer().contains("Mock response"))
          .verifyComplete();
    }

    @Test
    void chat_shouldHandleNewsAndWeatherTools() throws InterruptedException {
      // Mock News Tool Call
      String newsToolCall =
          "{\"message\": {\"role\": \"assistant\", \"tool_calls\": [{\"function\": {\"name\": \"search_news\", \"arguments\": {\"query\": \"tech\"}}}]}}";
      when(mcpClientService.callTool(eq("news"), eq("search"), any()))
          .thenReturn(Mono.just(objectMapper.createObjectNode().put("result", "news data")));

      // Mock Weather Tool Call
      String weatherToolCall =
          "{\"message\": {\"role\": \"assistant\", \"tool_calls\": [{\"function\": {\"name\": \"get_weather\", \"arguments\": {\"latitude\": 40.7}}}]}}";
      when(mcpClientService.callTool(eq("weather"), eq("get_forecast"), any()))
          .thenReturn(Mono.just(objectMapper.createObjectNode().put("temp", 22)));

      String finalResponse = "{\"message\": {\"role\": \"assistant\", \"content\": \"Done\"}}";

      mockWebServer.enqueue(new MockResponse().setBody(newsToolCall));
      mockWebServer.enqueue(new MockResponse().setBody(weatherToolCall));
      mockWebServer.enqueue(new MockResponse().setBody(finalResponse));

      StepVerifier.create(llmService.chat("News and weather?"))
          .expectNextMatches(response -> response.getEvidence().size() == 2)
          .verifyComplete();
    }

    @Test
    void chat_shouldStopAtMaxIterations() {
      // Enqueue more than 5 tool calls
      String loopResponse =
          "{\"message\": {\"role\": \"assistant\", \"content\": \"Thinking...\", \"tool_calls\": [{\"function\": {\"name\": \"get_quote\", \"arguments\": {\"symbol\": \"AAPL\"}}}]}}";
      when(mcpClientService.callTool(any(), any(), any()))
          .thenReturn(Mono.just(objectMapper.createObjectNode()));

      for (int i = 0; i < 6; i++) {
        mockWebServer.enqueue(new MockResponse().setBody(loopResponse));
      }

      StepVerifier.create(llmService.chat("Endless loop test"))
          .expectNextMatches(response -> response.getAnswer().equals("Thinking..."))
          .verifyComplete();
    }

    @Test
    void chat_shouldHandleToolExecutionErrorGracefully() {
      String toolCall =
          "{\"message\": {\"role\": \"assistant\", \"tool_calls\": [{\"function\": {\"name\": \"get_quote\", \"arguments\": {\"symbol\": \"FAIL\"}}}]}}";
      when(mcpClientService.callTool(any(), any(), any()))
          .thenReturn(Mono.error(new RuntimeException("MCP Down")));

      String finalResponse =
          "{\"message\": {\"role\": \"assistant\", \"content\": \"Handled error\"}}";

      mockWebServer.enqueue(new MockResponse().setBody(toolCall));
      mockWebServer.enqueue(new MockResponse().setBody(finalResponse));

      StepVerifier.create(llmService.chat("Trigger tool error"))
          .expectNextMatches(response -> response.getAnswer().equals("Handled error"))
          .verifyComplete();
    }

    @Test
    void chat_shouldHandleInvalidJsonResponse() {
      // Arrange: Enqueue invalid JSON
      mockWebServer.enqueue(new MockResponse().setBody("NOT_JSON"));

      // Act & Assert
      StepVerifier.create(llmService.chat("Invalid JSON test"))
          // Verify that the error is caught and converted to a ChatResponse (onNext)
          .expectNextMatches(
              response ->
                  response.getAnswer().contains("Mock response")
                      && response.getAnswer().contains("Unrecognized token 'NOT_JSON'"))
          .verifyComplete();
    }
  }
}
