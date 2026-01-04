package com.ai.livecontext.service;

import com.ai.livecontext.domain.NewsItem;
import com.ai.livecontext.domain.TimelineEvent;
import com.ai.livecontext.repository.NewsItemRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class IngestionService {

  private static final Logger logger = LoggerFactory.getLogger(IngestionService.class);

  private final McpClientService mcpClientService;
  private final TimelineService timelineService;
  private final NewsItemRepository newsItemRepository;
  private final ObjectMapper objectMapper;

  @Value("${livecontext.ingestion.news.enabled:true}")
  private boolean newsIngestionEnabled;

  @Value("${livecontext.ingestion.market.enabled:true}")
  private boolean marketIngestionEnabled;

  @Value("${livecontext.ingestion.weather.enabled:true}")
  private boolean weatherIngestionEnabled;

  @Value("#{'${livecontext.ingestion.news.feeds:}'.split(',')}")
  private List<String> newsFeeds;

  @Value("#{'${livecontext.ingestion.market.symbols:}'.split(',')}")
  private List<String> marketSymbols;

  public IngestionService(
      McpClientService mcpClientService,
      TimelineService timelineService,
      NewsItemRepository newsItemRepository,
      ObjectMapper objectMapper) {
    this.mcpClientService = mcpClientService;
    this.timelineService = timelineService;
    this.newsItemRepository = newsItemRepository;
    this.objectMapper = objectMapper;
  }

  @Scheduled(cron = "${livecontext.ingestion.news.cron:0 */5 * * * *}")
  public void ingestNews() {
    if (!newsIngestionEnabled) {
      return;
    }

    logger.info("[ingestion_news_start] Starting news ingestion | feedCount={}", newsFeeds.size());

    Flux.fromIterable(newsFeeds)
        .flatMap(
            feed -> {
              ObjectNode params = objectMapper.createObjectNode();
              params.put("feedUrl", feed);
              return mcpClientService.callTool("news", "ingest_rss", params);
            })
        .flatMap(
            result -> {
              return Flux.fromIterable(result.findValues("items"))
                  .flatMap(items -> Flux.fromIterable(items))
                  .map(
                      item ->
                          NewsItem.builder()
                              .id(item.path("id").asText())
                              .title(item.path("title").asText())
                              .link(item.path("link").asText())
                              .publishedAt(Instant.parse(item.path("publishedAt").asText()))
                              .source(item.path("source").asText())
                              .summary(item.path("summary").asText())
                              .createdAt(Instant.now())
                              .build());
            })
        .flatMap(
            newsItem ->
                newsItemRepository
                    .existsByLink(newsItem.getLink())
                    .flatMap(
                        exists -> {
                          if (!exists) {
                            return newsItemRepository
                                .save(newsItem)
                                .then(createNewsTimelineEvent(newsItem));
                          }
                          return Mono.empty();
                        }))
        .subscribe(
            success -> {},
            error ->
                logger.error(
                    "[ingestion_news_error] News ingestion failed | error={}", error.getMessage()),
            () -> logger.info("[ingestion_news_complete] News ingestion completed"));
  }

  @Scheduled(cron = "${livecontext.ingestion.market.cron:0 */1 * * * *}")
  public void ingestMarket() {
    if (!marketIngestionEnabled) {
      return;
    }

    logger.info(
        "[ingestion_market_start] Starting market ingestion | symbolCount={}",
        marketSymbols.size());

    Flux.fromIterable(marketSymbols)
        .flatMap(
            symbol -> {
              ObjectNode params = objectMapper.createObjectNode();
              params.put("symbol", symbol);
              params.put("interval", "daily");
              return mcpClientService.callTool("market", "get_quote", params);
            })
        .flatMap(this::createMarketTimelineEvent)
        .subscribe(
            success -> {},
            error ->
                logger.error(
                    "[ingestion_market_error] Market ingestion failed | error={}",
                    error.getMessage()),
            () -> logger.info("[ingestion_market_complete] Market ingestion completed"));
  }

  @Scheduled(cron = "${livecontext.ingestion.weather.cron:0 */15 * * * *}")
  public void ingestWeather() {
    if (!weatherIngestionEnabled) {
      return;
    }

    logger.info("[ingestion_weather_start] Starting weather ingestion");

    ObjectNode params = objectMapper.createObjectNode();
    params.put("latitude", 40.7128);
    params.put("longitude", -74.0060);

    mcpClientService
        .callTool("weather", "get_forecast", params)
        .flatMap(this::createWeatherTimelineEvent)
        .subscribe(
            success -> {},
            error ->
                logger.error(
                    "[ingestion_weather_error] Weather ingestion failed | error={}",
                    error.getMessage()),
            () -> logger.info("[ingestion_weather_complete] Weather ingestion completed"));
  }

  private Mono<Void> createNewsTimelineEvent(NewsItem newsItem) {
    ObjectNode payload = objectMapper.createObjectNode();
    payload.put("title", newsItem.getTitle());
    payload.put("link", newsItem.getLink());
    payload.put("source", newsItem.getSource());

    TimelineEvent event =
        TimelineEvent.builder()
            .eventType("news")
            .timestamp(newsItem.getPublishedAt())
            .payload(payload.toString())
            .createdAt(Instant.now())
            .build();

    return timelineService.addEvent(event).then();
  }

  private Mono<Void> createMarketTimelineEvent(JsonNode result) {
    TimelineEvent event =
        TimelineEvent.builder()
            .eventType("market")
            .timestamp(Instant.now())
            .payload(result.toString())
            .createdAt(Instant.now())
            .build();

    return timelineService.addEvent(event).then();
  }

  private Mono<Void> createWeatherTimelineEvent(JsonNode result) {
    TimelineEvent event =
        TimelineEvent.builder()
            .eventType("weather")
            .timestamp(Instant.now())
            .payload(result.toString())
            .createdAt(Instant.now())
            .build();

    return timelineService.addEvent(event).then();
  }
}
