package com.ai.livecontext.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class ChatRequestTest {

  @Test
  void builder_shouldCreateChatRequest() {
    ChatRequest request =
        ChatRequest.builder().question("What is the weather?").correlationId("corr-123").build();

    assertEquals("What is the weather?", request.getQuestion());
    assertEquals("corr-123", request.getCorrelationId());
  }

  @Test
  void setters_shouldModifyFields() {
    ChatRequest request = new ChatRequest();
    request.setQuestion("Test question");
    request.setCorrelationId("id-1");

    assertEquals("Test question", request.getQuestion());
    assertEquals("id-1", request.getCorrelationId());
  }

  @Test
  void noArgsConstructor_shouldWork() {
    ChatRequest request = new ChatRequest();
    assertNull(request.getQuestion());
    assertNull(request.getCorrelationId());
  }

  @Test
  void allArgsConstructor_shouldWork() {
    ChatRequest request = new ChatRequest("Question", "corr-id");
    assertEquals("Question", request.getQuestion());
    assertEquals("corr-id", request.getCorrelationId());
  }

  @Test
  void equals_shouldCompareCorrectly() {
    ChatRequest req1 = ChatRequest.builder().question("Q").correlationId("C").build();
    ChatRequest req2 = ChatRequest.builder().question("Q").correlationId("C").build();

    assertEquals(req1, req2);
    assertEquals(req1.hashCode(), req2.hashCode());
  }
}
