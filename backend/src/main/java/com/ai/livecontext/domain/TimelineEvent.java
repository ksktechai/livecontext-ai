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
@Table("timeline_events")
public class TimelineEvent {

  @Id private Long id;

  private String eventType;

  private Instant timestamp;

  private String payload; // Store as JSON string

  private String sources; // Store as JSON string

  private Instant createdAt;
}
