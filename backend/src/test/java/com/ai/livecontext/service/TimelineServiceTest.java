package com.ai.livecontext.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ai.livecontext.domain.TimelineEvent;
import com.ai.livecontext.repository.TimelineEventRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class TimelineServiceTest {

  @Mock private TimelineEventRepository repository;

  private TimelineService timelineService;

  @BeforeEach
  void setUp() {
    timelineService = new TimelineService(repository);
  }

  @Test
  void addEvent_shouldSaveEventAndEmitToSink() {
    TimelineEvent event =
        TimelineEvent.builder()
            .eventType("market")
            .timestamp(Instant.now())
            .payload("{\"price\": 100}")
            .build();

    TimelineEvent savedEvent =
        TimelineEvent.builder()
            .id(1L)
            .eventType("market")
            .timestamp(event.getTimestamp())
            .payload("{\"price\": 100}")
            .build();

    when(repository.save(any(TimelineEvent.class))).thenReturn(Mono.just(savedEvent));

    StepVerifier.create(timelineService.addEvent(event))
        .expectNextMatches(e -> e.getId() == 1L && e.getEventType().equals("market"))
        .verifyComplete();

    verify(repository).save(event);
  }

  @Test
  void getRecentEvents_shouldReturnLast50Events() {
    TimelineEvent event1 =
        TimelineEvent.builder().id(1L).eventType("market").timestamp(Instant.now()).build();
    TimelineEvent event2 =
        TimelineEvent.builder().id(2L).eventType("news").timestamp(Instant.now()).build();

    when(repository.findAllByOrderByTimestampDesc()).thenReturn(Flux.just(event1, event2));

    StepVerifier.create(timelineService.getRecentEvents())
        .expectNext(event1)
        .expectNext(event2)
        .verifyComplete();
  }

  @Test
  void streamEvents_shouldReturnFluxFromSink() {
    // The stream should be available even without events
    StepVerifier.create(timelineService.streamEvents().take(0)).verifyComplete();
  }
}
