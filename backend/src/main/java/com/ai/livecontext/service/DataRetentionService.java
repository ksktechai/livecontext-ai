package com.ai.livecontext.service;

import com.ai.livecontext.repository.NewsItemRepository;
import com.ai.livecontext.repository.TimelineEventRepository;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class DataRetentionService {

  private static final Logger logger = LoggerFactory.getLogger(DataRetentionService.class);

  private final TimelineEventRepository timelineEventRepository;
  private final NewsItemRepository newsItemRepository;

  @Value("${livecontext.retention.timeline-days:7}")
  private int timelineRetentionDays;

  @Value("${livecontext.retention.news-days:30}")
  private int newsRetentionDays;

  public DataRetentionService(
      TimelineEventRepository timelineEventRepository, NewsItemRepository newsItemRepository) {
    this.timelineEventRepository = timelineEventRepository;
    this.newsItemRepository = newsItemRepository;
  }

  @Scheduled(cron = "${livecontext.retention.cleanup-cron:0 0 2 * * *}")
  public void cleanupOldData() {
    logger.info("[data_retention_start] Starting scheduled data cleanup");

    cleanupTimelineEvents();
    cleanupNewsItems();
  }

  private void cleanupTimelineEvents() {
    Instant cutoff = Instant.now().minus(Duration.ofDays(timelineRetentionDays));

    timelineEventRepository
        .deleteByTimestampBefore(cutoff)
        .subscribe(
            count ->
                logger.info(
                    "[data_retention_timeline] Deleted old timeline events | count={} cutoffDays={}",
                    count,
                    timelineRetentionDays),
            error ->
                logger.error(
                    "[data_retention_timeline_error] Failed to cleanup timeline events | error={}",
                    error.getMessage()));
  }

  private void cleanupNewsItems() {
    Instant cutoff = Instant.now().minus(Duration.ofDays(newsRetentionDays));

    newsItemRepository
        .deleteByPublishedAtBefore(cutoff)
        .subscribe(
            count ->
                logger.info(
                    "[data_retention_news] Deleted old news items | count={} cutoffDays={}",
                    count,
                    newsRetentionDays),
            error ->
                logger.error(
                    "[data_retention_news_error] Failed to cleanup news items | error={}",
                    error.getMessage()));
  }
}
