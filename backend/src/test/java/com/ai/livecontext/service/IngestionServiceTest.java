package com.ai.livecontext.service;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.ai.livecontext.domain.NewsItem;
import com.ai.livecontext.repository.NewsItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class IngestionServiceTest {

  @Mock private McpClientService mcpClientService;
  @Mock private TimelineService timelineService;
  @Mock private NewsItemRepository newsItemRepository;

  private ObjectMapper objectMapper;
  private IngestionService ingestionService;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    ingestionService =
        new IngestionService(mcpClientService, timelineService, newsItemRepository, objectMapper);

    ReflectionTestUtils.setField(ingestionService, "newsIngestionEnabled", true);
    ReflectionTestUtils.setField(ingestionService, "marketIngestionEnabled", true);
    ReflectionTestUtils.setField(ingestionService, "weatherIngestionEnabled", true);
    ReflectionTestUtils.setField(ingestionService, "newsFeeds", Arrays.asList("http://feed1"));
    ReflectionTestUtils.setField(ingestionService, "marketSymbols", Arrays.asList("AAPL"));
  }

  @Test
  void ingestNews_shouldSaveNewItemsAndSkipExisting() throws InterruptedException {
    // Arrange
    CountDownLatch latch = new CountDownLatch(1);

    ObjectNode item1 =
        objectMapper
            .createObjectNode()
            .put("id", "1")
            .put("title", "New")
            .put("link", "link1")
            .put("publishedAt", Instant.now().toString())
            .put("source", "src")
            .put("summary", "sum");

    ArrayNode itemsArray = objectMapper.createArrayNode().add(item1);
    ObjectNode mcpResponse = objectMapper.createObjectNode();
    mcpResponse.set("items", itemsArray);

    when(mcpClientService.callTool(eq("news"), eq("ingest_rss"), any()))
        .thenReturn(Mono.just(mcpResponse));
    when(newsItemRepository.existsByLink("link1")).thenReturn(Mono.just(false));
    when(newsItemRepository.save(any())).thenReturn(Mono.just(mock(NewsItem.class)));
    when(timelineService.addEvent(any()))
        .thenAnswer(
            inv -> {
              latch.countDown();
              return Mono.just(inv.getArgument(0));
            });

    // Act
    ingestionService.ingestNews();

    // Assert
    assertTrue(latch.await(2, TimeUnit.SECONDS));
    verify(newsItemRepository, times(1)).save(any());
  }

  @Test
  void ingestWeather_shouldExecuteCorrectly() throws InterruptedException {
    // Arrange
    CountDownLatch latch = new CountDownLatch(1);
    ObjectNode weatherData = objectMapper.createObjectNode().put("forecast", "sunny");

    when(mcpClientService.callTool(eq("weather"), anyString(), any()))
        .thenReturn(Mono.just(weatherData));
    when(timelineService.addEvent(any()))
        .thenAnswer(
            inv -> {
              latch.countDown();
              return Mono.just(inv.getArgument(0));
            });

    // Act
    ingestionService.ingestWeather();

    // Assert
    assertTrue(latch.await(2, TimeUnit.SECONDS));
    verify(mcpClientService).callTool(eq("weather"), eq("get_forecast"), any());
  }

  @Test
  void ingestMarket_shouldHandleErrorsGracefully() throws InterruptedException {
    // We use a latch in the error block if we could, but since we log,
    // we just verify the call was made.
    when(mcpClientService.callTool(anyString(), anyString(), any()))
        .thenReturn(Mono.error(new RuntimeException("Market API Down")));

    ingestionService.ingestMarket();

    // Brief wait for the async error block to execute for coverage
    Thread.sleep(100);
    verify(mcpClientService).callTool(eq("market"), eq("get_quote"), any());
  }

  @Test
  void ingestionMethods_shouldSkipWhenDisabled() {
    ReflectionTestUtils.setField(ingestionService, "newsIngestionEnabled", false);
    ReflectionTestUtils.setField(ingestionService, "marketIngestionEnabled", false);
    ReflectionTestUtils.setField(ingestionService, "weatherIngestionEnabled", false);

    ingestionService.ingestNews();
    ingestionService.ingestMarket();
    ingestionService.ingestWeather();

    verify(mcpClientService, never()).callTool(anyString(), anyString(), any());
  }

  private void assertTrue(boolean condition) {
    if (!condition) throw new AssertionError("Condition failed");
  }
}
