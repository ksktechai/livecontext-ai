package com.ai.livecontext.provider;

import com.ai.livecontext.domain.MarketQuote;
import com.ai.livecontext.util.CorrelationIdHolder;
import java.io.StringReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class StooqMarketDataProvider implements MarketDataProvider {

  private static final Logger logger = LoggerFactory.getLogger(StooqMarketDataProvider.class);
  private static final String BASE_URL = "https://stooq.com/q/d/l/";
  private final WebClient webClient;

  public StooqMarketDataProvider() {
    this.webClient = WebClient.builder().baseUrl(BASE_URL).build();
  }

  @Override
  public Mono<List<MarketQuote>> getQuote(String symbol, String interval) {
    long startTime = System.currentTimeMillis();
    String normalizedSymbol = symbol.toLowerCase();
    String normalizedInterval = mapInterval(interval);
    String correlationId = CorrelationIdHolder.get();

    logger.info(
        "[market_fetch_start] Fetching market data from Stooq | provider=Stooq symbol={} interval={} correlationId={}",
        normalizedSymbol,
        normalizedInterval,
        correlationId);

    return webClient
        .get()
        .uri(
            uriBuilder ->
                uriBuilder
                    .queryParam("s", normalizedSymbol)
                    .queryParam("i", normalizedInterval)
                    .build())
        .retrieve()
        .bodyToMono(String.class)
        .map(csv -> parseCsv(csv, symbol))
        .doOnSuccess(
            quotes -> {
              long duration = System.currentTimeMillis() - startTime;
              logger.info(
                  "[market_fetch_success] Successfully fetched market data | provider=Stooq symbol={} count={} duration_ms={} correlationId={}",
                  normalizedSymbol,
                  quotes.size(),
                  duration,
                  correlationId);
            })
        .doOnError(
            error -> {
              long duration = System.currentTimeMillis() - startTime;
              logger.error(
                  "[market_fetch_error] Failed to fetch market data | provider=Stooq symbol={} error={} duration_ms={} correlationId={}",
                  normalizedSymbol,
                  error.getMessage(),
                  duration,
                  correlationId);
            });
  }

  private String mapInterval(String interval) {
    return switch (interval.toLowerCase()) {
      case "daily", "d" -> "d";
      case "weekly", "w" -> "w";
      case "monthly", "m" -> "m";
      default -> "d";
    };
  }

  private List<MarketQuote> parseCsv(String csvData, String symbol) {
    List<MarketQuote> quotes = new ArrayList<>();
    try (CSVParser parser =
        CSVParser.parse(
            new StringReader(csvData),
            CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).build())) {

      for (CSVRecord record : parser) {
        MarketQuote quote =
            MarketQuote.builder()
                .symbol(symbol)
                .date(LocalDate.parse(record.get("Date"), DateTimeFormatter.ISO_LOCAL_DATE))
                .open(new BigDecimal(record.get("Open")))
                .high(new BigDecimal(record.get("High")))
                .low(new BigDecimal(record.get("Low")))
                .close(new BigDecimal(record.get("Close")))
                .volume(Long.parseLong(record.get("Volume")))
                .build();
        quotes.add(quote);
      }
    } catch (Exception e) {
      logger.error(
          "[csv_parse_error] Failed to parse CSV data | symbol={} error={}",
          symbol,
          e.getMessage());
      throw new RuntimeException("Failed to parse market data CSV", e);
    }
    return quotes;
  }
}
