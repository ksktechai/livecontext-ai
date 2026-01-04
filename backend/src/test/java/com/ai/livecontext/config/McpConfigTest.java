package com.ai.livecontext.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class McpConfigTest {

  @Test
  void mcpEndpoint_shouldHaveCorrectDefaults() {
    McpConfig.McpEndpoint endpoint = new McpConfig.McpEndpoint();
    endpoint.setUrl("http://localhost:8091");
    endpoint.setTimeout(5000);

    assertEquals("http://localhost:8091", endpoint.getUrl());
    assertEquals(5000, endpoint.getTimeout());
  }

  @Test
  void mcpConfig_shouldContainAllServers() {
    McpConfig mcpConfig = new McpConfig();

    McpConfig.McpEndpoint market = new McpConfig.McpEndpoint();
    market.setUrl("http://localhost:8091");
    market.setTimeout(10000);

    McpConfig.McpEndpoint news = new McpConfig.McpEndpoint();
    news.setUrl("http://localhost:8092");
    news.setTimeout(15000);

    mcpConfig.setMarket(market);
    mcpConfig.setNews(news);

    assertEquals("http://localhost:8091", mcpConfig.getMarket().getUrl());
    assertEquals("http://localhost:8092", mcpConfig.getNews().getUrl());
  }

  @Test
  void mcpEndpoint_shouldHaveDefaultTimeout() {
    McpConfig.McpEndpoint endpoint = new McpConfig.McpEndpoint();
    assertEquals(10000, endpoint.getTimeout());
  }
}
