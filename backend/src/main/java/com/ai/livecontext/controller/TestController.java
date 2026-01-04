package com.ai.livecontext.controller;

import com.ai.livecontext.service.McpClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/test")
public class TestController {

  private final McpClientService mcpClientService;
  private final ObjectMapper objectMapper;

  public TestController(McpClientService mcpClientService, ObjectMapper objectMapper) {
    this.mcpClientService = mcpClientService;
    this.objectMapper = objectMapper;
  }

  @GetMapping("/market/{symbol}")
  public Mono<Object> testMarketQuote(@PathVariable String symbol) {
    ObjectNode params = objectMapper.createObjectNode();
    params.put("symbol", symbol);
    return mcpClientService
        .callTool("market", "get_quote", params)
        .map(jsonNode -> objectMapper.convertValue(jsonNode, Map.class));
  }

  @GetMapping("/news")
  public Mono<Object> testNewsSearch(@RequestParam(defaultValue = "technology") String query) {
    ObjectNode params = objectMapper.createObjectNode();
    params.put("query", query);
    params.put("limit", 5);
    return mcpClientService
        .callTool("news", "search", params)
        .map(jsonNode -> objectMapper.convertValue(jsonNode, Map.class));
  }

  @GetMapping("/weather")
  public Mono<Object> testWeatherForecast(
      @RequestParam(defaultValue = "40.7128") double latitude,
      @RequestParam(defaultValue = "-74.0060") double longitude) {
    ObjectNode params = objectMapper.createObjectNode();
    params.put("latitude", latitude);
    params.put("longitude", longitude);
    return mcpClientService
        .callTool("weather", "get_forecast", params)
        .map(jsonNode -> objectMapper.convertValue(jsonNode, Map.class));
  }

  @GetMapping("/health")
  public Mono<Map<String, String>> health() {
    return Mono.just(
        Map.of(
            "status",
            "healthy",
            "message",
            "Test endpoints available",
            "endpoints",
            "/api/test/market/MSFT, /api/test/news, /api/test/weather"));
  }
}
