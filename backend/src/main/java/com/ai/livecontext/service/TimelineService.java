package com.ai.livecontext.service;

import com.ai.livecontext.domain.TimelineEvent;
import com.ai.livecontext.repository.TimelineEventRepository;
import java.time.Duration;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

@Service
public class TimelineService {

  private final TimelineEventRepository repository;
  private final Sinks.Many<TimelineEvent> sink;

  public TimelineService(TimelineEventRepository repository) {
    this.repository = repository;
    this.sink = Sinks.many().multicast().onBackpressureBuffer();
  }

  public Mono<TimelineEvent> addEvent(TimelineEvent event) {
    return repository.save(event).doOnSuccess(saved -> sink.tryEmitNext(saved));
  }

  public Flux<TimelineEvent> streamEvents() {
    return sink.asFlux().delayElements(Duration.ofMillis(100));
  }

  public Flux<TimelineEvent> getRecentEvents() {
    return repository.findAllByOrderByTimestampDesc().take(50);
  }
}
