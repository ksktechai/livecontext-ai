package com.ai.livecontext.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "livecontext.mcp")
public class McpConfig {

  private McpEndpoint market = new McpEndpoint();
  private McpEndpoint news = new McpEndpoint();
  private McpEndpoint weather = new McpEndpoint();
  private McpEndpoint system = new McpEndpoint();

  @Data
  public static class McpEndpoint {
    private String url;
    private int timeout = 10000;
  }
}
