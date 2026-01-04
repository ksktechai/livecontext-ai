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
@Table("alert_rules")
public class AlertRule {

  @Id private Long id;

  private String name;

  private String condition; // Store as JSON string

  private Boolean enabled;

  private Instant createdAt;
}
