package com.ai.livecontext.domain;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("alert_events")
public class AlertEvent {

  @Id private Long id;

  private Long ruleId;

  private Instant triggeredAt;

  private String payload; // Store as JSON string

  private Instant createdAt;
}
