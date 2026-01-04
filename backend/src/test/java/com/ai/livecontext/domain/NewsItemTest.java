package com.ai.livecontext.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import org.junit.jupiter.api.Test;

class NewsItemTest {

  @Test
  void builder_shouldCreateNewsItem() {
    Instant now = Instant.now();
    NewsItem item =
        NewsItem.builder()
            .id("news-123")
            .title("Test News")
            .link("https://example.com/news")
            .publishedAt(now)
            .source("reddit")
            .summary("Test summary")
            .build();

    assertEquals("news-123", item.getId());
    assertEquals("Test News", item.getTitle());
    assertEquals("https://example.com/news", item.getLink());
    assertEquals(now, item.getPublishedAt());
    assertEquals("reddit", item.getSource());
    assertEquals("Test summary", item.getSummary());
  }

  @Test
  void setters_shouldModifyFields() {
    NewsItem item = new NewsItem();
    item.setId("id-1");
    item.setTitle("Title");
    item.setLink("https://link.com");

    assertEquals("id-1", item.getId());
    assertEquals("Title", item.getTitle());
    assertEquals("https://link.com", item.getLink());
  }

  @Test
  void equals_shouldWork() {
    NewsItem item1 = NewsItem.builder().id("id-1").title("Title").build();
    NewsItem item2 = NewsItem.builder().id("id-1").title("Title").build();

    assertEquals(item1, item2);
  }
}
