package com.ai.livecontext.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.ai.livecontext.repository.NewsItemRepository;
import com.ai.livecontext.repository.TimelineEventRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class DataRetentionServiceTest {

  @Mock private TimelineEventRepository timelineEventRepository;
  @Mock private NewsItemRepository newsItemRepository;

  private DataRetentionService dataRetentionService;

  @BeforeEach
  void setUp() {
    dataRetentionService = new DataRetentionService(timelineEventRepository, newsItemRepository);
    ReflectionTestUtils.setField(dataRetentionService, "timelineRetentionDays", 7);
    ReflectionTestUtils.setField(dataRetentionService, "newsRetentionDays", 30);
  }

  @Test
  void cleanupOldData_shouldDeleteOldTimelineEvents() {
    when(timelineEventRepository.deleteByTimestampBefore(any(Instant.class)))
        .thenReturn(Mono.just(10));
    when(newsItemRepository.deleteByPublishedAtBefore(any(Instant.class))).thenReturn(Mono.just(5));

    // Call the cleanup method
    dataRetentionService.cleanupOldData();

    // Give it time to complete async operations
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }

    verify(timelineEventRepository).deleteByTimestampBefore(any(Instant.class));
    verify(newsItemRepository).deleteByPublishedAtBefore(any(Instant.class));
  }
}
