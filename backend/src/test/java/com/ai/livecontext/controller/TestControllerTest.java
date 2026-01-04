package com.ai.livecontext.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.ai.livecontext.service.McpClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class TestControllerTest {

  @Mock private McpClientService mcpClientService;

  private ObjectMapper objectMapper;
  private TestController testController;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    testController = new TestController(mcpClientService, objectMapper);
  }

  @Test
  void testMarketQuote_shouldCallMcpAndReturnResponse() {
    ObjectNode response = objectMapper.createObjectNode();
    response.put("symbol", "AAPL");
    response.put("price", 150.00);

    when(mcpClientService.callTool(eq("market"), eq("get_quote"), any()))
        .thenReturn(Mono.just(response));

    StepVerifier.create(testController.testMarketQuote("AAPL"))
        .expectNextMatches(result -> result instanceof Map)
        .verifyComplete();

    verify(mcpClientService).callTool(eq("market"), eq("get_quote"), any());
  }

  @Test
  void testNewsSearch_shouldCallMcpAndReturnResponse() {
    ObjectNode response = objectMapper.createObjectNode();
    response.put("count", 10);

    when(mcpClientService.callTool(eq("news"), eq("search"), any()))
        .thenReturn(Mono.just(response));

    StepVerifier.create(testController.testNewsSearch("technology"))
        .expectNextMatches(result -> result instanceof Map)
        .verifyComplete();

    verify(mcpClientService).callTool(eq("news"), eq("search"), any());
  }

  @Test
  void testWeatherForecast_shouldCallMcpAndReturnResponse() {
    ObjectNode response = objectMapper.createObjectNode();
    response.put("temperature", 25.5);

    when(mcpClientService.callTool(eq("weather"), eq("get_forecast"), any()))
        .thenReturn(Mono.just(response));

    StepVerifier.create(testController.testWeatherForecast(40.7128, -74.0060))
        .expectNextMatches(result -> result instanceof Map)
        .verifyComplete();

    verify(mcpClientService).callTool(eq("weather"), eq("get_forecast"), any());
  }

  @Test
  void health_shouldReturnStatus() {
    StepVerifier.create(testController.health())
        .expectNextMatches(result -> "healthy".equals(result.get("status")))
        .verifyComplete();
  }

  @Test
  void testMarketQuote_shouldHandleError() {
    when(mcpClientService.callTool(eq("market"), eq("get_quote"), any()))
        .thenReturn(Mono.error(new RuntimeException("MCP error")));

    StepVerifier.create(testController.testMarketQuote("AAPL"))
        .expectError(RuntimeException.class)
        .verify();
  }
}
