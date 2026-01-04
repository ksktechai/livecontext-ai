package com.ai.livecontext.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class McpConfigAdditionalTest {

  @Test
  void mcpConfig_shouldHaveDefaultEndpoints() {
    McpConfig config = new McpConfig();

    assertNotNull(config.getMarket());
    assertNotNull(config.getNews());
    assertNotNull(config.getWeather());
    assertNotNull(config.getSystem());
  }

  @Test
  void mcpEndpoint_shouldAllowAllFields() {
    McpConfig.McpEndpoint endpoint = new McpConfig.McpEndpoint();
    endpoint.setUrl("http://localhost:9000");
    endpoint.setTimeout(30000);

    assertEquals("http://localhost:9000", endpoint.getUrl());
    assertEquals(30000, endpoint.getTimeout());
  }

  @Test
  void mcpConfig_shouldSetWeather() {
    McpConfig config = new McpConfig();
    McpConfig.McpEndpoint weather = new McpConfig.McpEndpoint();
    weather.setUrl("http://localhost:8093");

    config.setWeather(weather);

    assertEquals("http://localhost:8093", config.getWeather().getUrl());
  }

  @Test
  void mcpConfig_shouldSetSystem() {
    McpConfig config = new McpConfig();
    McpConfig.McpEndpoint system = new McpConfig.McpEndpoint();
    system.setUrl("http://localhost:8094");

    config.setSystem(system);

    assertEquals("http://localhost:8094", config.getSystem().getUrl());
  }

  @Test
  void mcpEndpoint_equals_shouldWork() {
    McpConfig.McpEndpoint ep1 = new McpConfig.McpEndpoint();
    ep1.setUrl("http://localhost:8091");
    ep1.setTimeout(5000);

    McpConfig.McpEndpoint ep2 = new McpConfig.McpEndpoint();
    ep2.setUrl("http://localhost:8091");
    ep2.setTimeout(5000);

    assertEquals(ep1, ep2);
    assertEquals(ep1.hashCode(), ep2.hashCode());
  }

  @Test
  void mcpConfig_toString_shouldWork() {
    McpConfig config = new McpConfig();
    assertNotNull(config.toString());
    assertTrue(config.toString().contains("McpConfig"));
  }
}
