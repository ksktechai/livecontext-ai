package com.ai.livecontext.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class AlertEventTest {

  @Test
  void builder_shouldCreateAlertEvent() {
    Instant now = Instant.now();
    AlertEvent event =
        AlertEvent.builder()
            .id(1L)
            .ruleId(10L)
            .triggeredAt(now)
            .payload("{\"alert\": true}")
            .build();

    assertEquals(1L, event.getId());
    assertEquals(10L, event.getRuleId());
    assertEquals(now, event.getTriggeredAt());
    assertEquals("{\"alert\": true}", event.getPayload());
  }

  @Test
  void setters_shouldModifyFields() {
    AlertEvent event = new AlertEvent();
    event.setId(2L);
    event.setRuleId(20L);
    event.setPayload("{\"data\": \"test\"}");

    assertEquals(2L, event.getId());
    assertEquals(20L, event.getRuleId());
    assertEquals("{\"data\": \"test\"}", event.getPayload());
  }

  @Test
  void noArgsConstructor_shouldWork() {
    AlertEvent event = new AlertEvent();
    assertNull(event.getId());
    assertNull(event.getRuleId());
  }
}
