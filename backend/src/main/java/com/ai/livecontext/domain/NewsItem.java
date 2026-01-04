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
@Table("news_items")
public class NewsItem {

  @Id private String id;

  private String title;

  private String link;

  private Instant publishedAt;

  private String source;

  private String summary;

  private Instant createdAt;
}
