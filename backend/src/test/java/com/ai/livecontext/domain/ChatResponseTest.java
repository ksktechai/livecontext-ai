package com.ai.livecontext.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ChatResponseTest {

  @Test
  void builder_shouldCreateChatResponse() {
    List<ChatResponse.Evidence> evidence =
        Arrays.asList(
            ChatResponse.Evidence.builder()
                .type("market")
                .source("stooq")
                .timestamp("2024-01-01T00:00:00Z")
                .summary("AAPL price data")
                .build());

    ChatResponse response =
        ChatResponse.builder()
            .answer("The current price is $150")
            .evidence(evidence)
            .correlationId("test-123")
            .build();

    assertEquals("The current price is $150", response.getAnswer());
    assertEquals(1, response.getEvidence().size());
    assertEquals("test-123", response.getCorrelationId());
  }

  @Test
  void evidence_shouldBuildCorrectly() {
    ChatResponse.Evidence evidence =
        ChatResponse.Evidence.builder()
            .type("weather")
            .source("open-meteo")
            .timestamp("2024-01-01T00:00:00Z")
            .summary("Weather forecast")
            .build();

    assertEquals("weather", evidence.getType());
    assertEquals("open-meteo", evidence.getSource());
    assertEquals("Weather forecast", evidence.getSummary());
  }

  @Test
  void setters_shouldWork() {
    ChatResponse response = new ChatResponse();
    response.setAnswer("Test answer");
    response.setCorrelationId("id-1");

    assertEquals("Test answer", response.getAnswer());
    assertEquals("id-1", response.getCorrelationId());
  }

  @Test
  void evidenceSetters_shouldWork() {
    ChatResponse.Evidence evidence = new ChatResponse.Evidence();
    evidence.setType("news");
    evidence.setSource("reddit");
    evidence.setTimestamp("2024-01-01");
    evidence.setSummary("News summary");

    assertEquals("news", evidence.getType());
    assertEquals("reddit", evidence.getSource());
  }
}
