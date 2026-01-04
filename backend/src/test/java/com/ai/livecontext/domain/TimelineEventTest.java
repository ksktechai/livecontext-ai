package com.ai.livecontext.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class TimelineEventTest {

  @Test
  void builder_shouldCreateTimelineEvent() {
    Instant now = Instant.now();
    TimelineEvent event =
        TimelineEvent.builder()
            .id(1L)
            .eventType("market")
            .timestamp(now)
            .payload("{\"price\": 100}")
            .sources("stooq")
            .build();

    assertEquals(1L, event.getId());
    assertEquals("market", event.getEventType());
    assertEquals(now, event.getTimestamp());
    assertEquals("{\"price\": 100}", event.getPayload());
    assertEquals("stooq", event.getSources());
  }

  @Test
  void setters_shouldModifyFields() {
    TimelineEvent event = new TimelineEvent();
    event.setId(2L);
    event.setEventType("news");
    event.setPayload("{\"title\": \"Test\"}");

    assertEquals(2L, event.getId());
    assertEquals("news", event.getEventType());
    assertEquals("{\"title\": \"Test\"}", event.getPayload());
  }

  @Test
  void equals_shouldCompareCorrectly() {
    TimelineEvent event1 = TimelineEvent.builder().id(1L).eventType("market").build();
    TimelineEvent event2 = TimelineEvent.builder().id(1L).eventType("market").build();

    assertEquals(event1, event2);
    assertEquals(event1.hashCode(), event2.hashCode());
  }

  @Test
  void toString_shouldReturnString() {
    TimelineEvent event = TimelineEvent.builder().id(1L).build();
    assertNotNull(event.toString());
    assertTrue(event.toString().contains("id=1"));
  }
}
