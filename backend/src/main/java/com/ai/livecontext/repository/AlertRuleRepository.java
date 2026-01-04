package com.ai.livecontext.repository;

import com.ai.livecontext.domain.AlertRule;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AlertRuleRepository extends R2dbcRepository<AlertRule, Long> {

  Flux<AlertRule> findByEnabledTrue();
}
