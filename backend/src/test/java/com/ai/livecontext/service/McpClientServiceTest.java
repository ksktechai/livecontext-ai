package com.ai.livecontext.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.lenient;

import com.ai.livecontext.config.McpConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class McpClientServiceTest {

  private MockWebServer mockWebServer;
  private McpClientService mcpClientService;
  private ObjectMapper objectMapper;

  @Mock private McpConfig mcpConfig;
  @Mock private McpConfig.McpEndpoint marketEndpoint;

  @BeforeEach
  void setUp() throws IOException {
    mockWebServer = new MockWebServer();
    mockWebServer.start();
    objectMapper = new ObjectMapper();

    String serverUrl = mockWebServer.url("/").toString().replaceAll("/$", "");

    lenient().when(mcpConfig.getMarket()).thenReturn(marketEndpoint);
    lenient().when(marketEndpoint.getUrl()).thenReturn(serverUrl);
    lenient().when(marketEndpoint.getTimeout()).thenReturn(5000);

    mcpClientService = new McpClientService(mcpConfig, objectMapper);
  }

  @AfterEach
  void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  @Test
  void callTool_shouldReturnJsonResponseOnSuccess() {
    String responseBody = "{\"symbol\":\"AAPL\",\"price\":150.0}";
    mockWebServer.enqueue(
        new MockResponse().setBody(responseBody).addHeader("Content-Type", "application/json"));

    ObjectNode params = objectMapper.createObjectNode();
    params.put("symbol", "AAPL");

    StepVerifier.create(mcpClientService.callTool("market", "get_quote", params))
        .expectNextMatches(json -> json.has("symbol") && json.get("symbol").asText().equals("AAPL"))
        .verifyComplete();
  }

  @Test
  void callTool_shouldHandleServerError() {
    mockWebServer.enqueue(new MockResponse().setResponseCode(500).setBody("Server Error"));

    ObjectNode params = objectMapper.createObjectNode();
    params.put("symbol", "AAPL");

    StepVerifier.create(mcpClientService.callTool("market", "get_quote", params))
        .expectError()
        .verify();
  }

  @Test
  void callTool_shouldIncludeCorrelationIdInRequest() throws InterruptedException {
    String correlationId = "test-uuid-123";
    com.ai.livecontext.util.CorrelationIdHolder.set(correlationId);

    mockWebServer.enqueue(new MockResponse().setBody("{}"));

    StepVerifier.create(
            mcpClientService.callTool("market", "test", objectMapper.createObjectNode()))
        .expectNextCount(1)
        .verifyComplete();

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getBody().readUtf8()).contains(correlationId);

    com.ai.livecontext.util.CorrelationIdHolder.clear();
  }

  @Test
  void callTool_shouldHandleTimeout() {
    // Set a very short timeout for testing
    lenient().when(marketEndpoint.getTimeout()).thenReturn(100);

    mockWebServer.enqueue(
        new MockResponse()
            .setBody("{}")
            .setBodyDelay(500, java.util.concurrent.TimeUnit.MILLISECONDS));

    StepVerifier.create(
            mcpClientService.callTool("market", "test", objectMapper.createObjectNode()))
        .expectError(java.util.concurrent.TimeoutException.class)
        .verify();
  }

  @Test
  void callTool_shouldHandleMalformedJsonResponse() {
    mockWebServer.enqueue(new MockResponse().setBody("INVALID_JSON"));

    StepVerifier.create(
            mcpClientService.callTool("market", "test", objectMapper.createObjectNode()))
        .expectError(com.fasterxml.jackson.core.JsonParseException.class)
        .verify();
  }

  @Test
  void callTool_shouldSwitchCorrectServerUrls() {
    // Mock other endpoints
    McpConfig.McpEndpoint newsEndpoint = mock(McpConfig.McpEndpoint.class);
    McpConfig.McpEndpoint weatherEndpoint = mock(McpConfig.McpEndpoint.class);
    McpConfig.McpEndpoint systemEndpoint = mock(McpConfig.McpEndpoint.class);

    when(mcpConfig.getNews()).thenReturn(newsEndpoint);
    when(mcpConfig.getWeather()).thenReturn(weatherEndpoint);
    when(mcpConfig.getSystem()).thenReturn(systemEndpoint);

    when(newsEndpoint.getUrl()).thenReturn("http://news-server");
    when(weatherEndpoint.getUrl()).thenReturn("http://weather-server");
    when(systemEndpoint.getUrl()).thenReturn("http://system-server");

    // We only need to check that the URL generation logic doesn't throw and matches the switch
    // Internal getServerUrl is private, so we test via the public callTool error (due to bad URL)
    StepVerifier.create(mcpClientService.callTool("news", "test", objectMapper.createObjectNode()))
        .expectError() // Fails because http://news-server isn't our MockWebServer
        .verify();

    StepVerifier.create(
            mcpClientService.callTool("weather", "test", objectMapper.createObjectNode()))
        .expectError()
        .verify();

    StepVerifier.create(
            mcpClientService.callTool("system", "test", objectMapper.createObjectNode()))
        .expectError()
        .verify();
  }

  @Test
  void callTool_shouldThrowForUnknownServer() {
    ObjectNode params = objectMapper.createObjectNode();

    org.junit.jupiter.api.Assertions.assertThrows(
        IllegalArgumentException.class, () -> mcpClientService.callTool("unknown", "test", params));
  }
}
