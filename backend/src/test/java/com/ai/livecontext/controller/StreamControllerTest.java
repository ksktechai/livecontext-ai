package com.ai.livecontext.controller;

import static org.mockito.Mockito.*;

import com.ai.livecontext.domain.TimelineEvent;
import com.ai.livecontext.service.TimelineService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class StreamControllerTest {

  @Mock private TimelineService timelineService;

  private StreamController streamController;

  @BeforeEach
  void setUp() {
    streamController = new StreamController(timelineService);
  }

  @Test
  void getRecentEvents_shouldReturnEvents() {
    TimelineEvent event1 =
        TimelineEvent.builder()
            .id(1L)
            .eventType("market")
            .timestamp(Instant.now())
            .payload("{\"price\": 150}")
            .build();

    when(timelineService.getRecentEvents()).thenReturn(Flux.just(event1));

    StepVerifier.create(streamController.getRecentEvents())
        .expectNextMatches(e -> e.getId() == 1L && e.getEventType().equals("market"))
        .verifyComplete();
  }

  @Test
  void streamTimeline_shouldReturnSSEStreamFromService() {
    TimelineEvent event =
        TimelineEvent.builder()
            .id(1L)
            .eventType("news")
            .timestamp(Instant.now())
            .payload("{}")
            .build();

    when(timelineService.streamEvents()).thenReturn(Flux.just(event));

    StepVerifier.create(streamController.streamTimeline())
        .expectNextMatches(sse -> sse.data().getId() == 1L)
        .verifyComplete();
  }
}
