package com.ai.livecontext.repository;

import com.ai.livecontext.domain.NewsItem;
import java.time.Instant;
import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface NewsItemRepository extends R2dbcRepository<NewsItem, String> {

  Flux<NewsItem> findAllByOrderByPublishedAtDesc();

  @Query(
      "SELECT * FROM news_items WHERE title ILIKE '%' || :keyword || '%' ORDER BY published_at"
          + " DESC LIMIT 10")
  Flux<NewsItem> searchByKeyword(String keyword);

  Mono<Boolean> existsByLink(String link);

  @Modifying
  @Query("DELETE FROM news_items WHERE published_at < :cutoff")
  Mono<Integer> deleteByPublishedAtBefore(Instant cutoff);
}
