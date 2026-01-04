package com.ai.livecontext.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ai.livecontext.domain.AlertRule;
import com.ai.livecontext.service.AlertService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
class AlertControllerTest {

  @Mock private AlertService alertService;

  private AlertController alertController;

  @BeforeEach
  void setUp() {
    alertController = new AlertController(alertService);
  }

  @Test
  void createAlert_shouldReturnCreatedRule() {
    AlertRule rule =
        AlertRule.builder().name("Test Alert").condition("price > 100").enabled(true).build();

    AlertRule savedRule =
        AlertRule.builder()
            .id(1L)
            .name("Test Alert")
            .condition("price > 100")
            .enabled(true)
            .build();

    when(alertService.createAlert(any(AlertRule.class))).thenReturn(Mono.just(savedRule));

    StepVerifier.create(alertController.createAlert(rule))
        .expectNextMatches(r -> r.getId() == 1L && r.getName().equals("Test Alert"))
        .verifyComplete();
  }

  @Test
  void getAlerts_shouldReturnAllRules() {
    AlertRule rule1 = AlertRule.builder().id(1L).name("Alert 1").build();
    AlertRule rule2 = AlertRule.builder().id(2L).name("Alert 2").build();

    when(alertService.getAllAlerts()).thenReturn(Flux.just(rule1, rule2));

    StepVerifier.create(alertController.getAlerts())
        .expectNext(rule1)
        .expectNext(rule2)
        .verifyComplete();
  }
}
