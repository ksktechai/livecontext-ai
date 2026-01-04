package com.ai.livecontext.controller;

import com.ai.livecontext.domain.TimelineEvent;
import com.ai.livecontext.service.TimelineService;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api")
public class StreamController {

  private final TimelineService timelineService;

  public StreamController(TimelineService timelineService) {
    this.timelineService = timelineService;
  }

  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<ServerSentEvent<TimelineEvent>> streamTimeline() {
    return timelineService
        .streamEvents()
        .map(
            event ->
                ServerSentEvent.builder(event)
                    .id(event.getId().toString())
                    .event("timeline")
                    .build());
  }

  @GetMapping("/timeline/recent")
  public Flux<TimelineEvent> getRecentEvents() {
    return timelineService.getRecentEvents();
  }
}
