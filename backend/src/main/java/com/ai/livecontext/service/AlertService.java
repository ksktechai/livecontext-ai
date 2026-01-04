package com.ai.livecontext.service;

import com.ai.livecontext.domain.AlertRule;
import com.ai.livecontext.repository.AlertRuleRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class AlertService {

  private final AlertRuleRepository repository;

  public AlertService(AlertRuleRepository repository) {
    this.repository = repository;
  }

  public Mono<AlertRule> createAlert(AlertRule rule) {
    return repository.save(rule);
  }

  public Flux<AlertRule> getAllAlerts() {
    return repository.findAll();
  }

  public Flux<AlertRule> getEnabledAlerts() {
    return repository.findByEnabledTrue();
  }
}
