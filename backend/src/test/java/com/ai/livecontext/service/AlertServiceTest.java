package com.ai.livecontext.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ai.livecontext.domain.AlertRule;
import com.ai.livecontext.repository.AlertRuleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AlertServiceTest {

  @Mock private AlertRuleRepository repository;

  private AlertService alertService;

  @BeforeEach
  void setUp() {
    alertService = new AlertService(repository);
  }

  @Test
  void createAlert_shouldSaveAndReturnRule() {
    AlertRule rule =
        AlertRule.builder().name("Test Alert").condition("price > 100").enabled(true).build();

    AlertRule savedRule =
        AlertRule.builder()
            .id(1L)
            .name("Test Alert")
            .condition("price > 100")
            .enabled(true)
            .build();

    when(repository.save(any(AlertRule.class))).thenReturn(Mono.just(savedRule));

    StepVerifier.create(alertService.createAlert(rule))
        .expectNextMatches(r -> r.getId() == 1L && r.getName().equals("Test Alert"))
        .verifyComplete();

    verify(repository).save(rule);
  }

  @Test
  void getAllAlerts_shouldReturnAllRules() {
    AlertRule rule1 = AlertRule.builder().id(1L).name("Alert 1").build();
    AlertRule rule2 = AlertRule.builder().id(2L).name("Alert 2").build();

    when(repository.findAll()).thenReturn(Flux.just(rule1, rule2));

    StepVerifier.create(alertService.getAllAlerts())
        .expectNext(rule1)
        .expectNext(rule2)
        .verifyComplete();
  }

  @Test
  void getEnabledAlerts_shouldReturnOnlyEnabled() {
    AlertRule enabledRule = AlertRule.builder().id(1L).name("Enabled").enabled(true).build();

    when(repository.findByEnabledTrue()).thenReturn(Flux.just(enabledRule));

    StepVerifier.create(alertService.getEnabledAlerts())
        .expectNextMatches(r -> r.getEnabled())
        .verifyComplete();
  }
}
