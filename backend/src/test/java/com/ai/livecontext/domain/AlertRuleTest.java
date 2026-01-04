package com.ai.livecontext.domain;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AlertRuleTest {

  @Test
  void builder_shouldCreateAlertRule() {
    AlertRule rule =
        AlertRule.builder()
            .id(1L)
            .name("Test Alert")
            .condition("{\"symbol\": \"AAPL\", \"threshold\": 150}")
            .enabled(true)
            .build();

    assertEquals(1L, rule.getId());
    assertEquals("Test Alert", rule.getName());
    assertTrue(rule.getEnabled());
  }

  @Test
  void setters_shouldModifyFields() {
    AlertRule rule = new AlertRule();
    rule.setId(2L);
    rule.setName("Updated Alert");
    rule.setCondition("{\"type\": \"price\"}");
    rule.setEnabled(false);

    assertEquals(2L, rule.getId());
    assertEquals("Updated Alert", rule.getName());
    assertFalse(rule.getEnabled());
  }

  @Test
  void noArgsConstructor_shouldWork() {
    AlertRule rule = new AlertRule();
    assertNull(rule.getId());
    assertNull(rule.getName());
  }

  @Test
  void allArgsConstructor_shouldWork() {
    AlertRule rule = new AlertRule(1L, "Name", "Condition", true, null);
    assertEquals(1L, rule.getId());
    assertEquals("Name", rule.getName());
  }
}
