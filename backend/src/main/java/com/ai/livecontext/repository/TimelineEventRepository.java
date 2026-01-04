package com.ai.livecontext.repository;

import com.ai.livecontext.domain.TimelineEvent;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface TimelineEventRepository extends R2dbcRepository<TimelineEvent, Long> {

  Flux<TimelineEvent> findAllByOrderByTimestampDesc();

  @Modifying
  @Query("DELETE FROM timeline_events WHERE timestamp < :cutoff")
  Mono<Integer> deleteByTimestampBefore(Instant cutoff);
}
