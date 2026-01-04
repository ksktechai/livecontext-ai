package com.ai.livecontext.controller;

import com.ai.livecontext.domain.AlertRule;
import com.ai.livecontext.service.AlertService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

  private final AlertService alertService;

  public AlertController(AlertService alertService) {
    this.alertService = alertService;
  }

  @PostMapping
  public Mono<AlertRule> createAlert(@Valid @RequestBody AlertRule rule) {
    return alertService.createAlert(rule);
  }

  @GetMapping
  public Flux<AlertRule> getAlerts() {
    return alertService.getAllAlerts();
  }
}
