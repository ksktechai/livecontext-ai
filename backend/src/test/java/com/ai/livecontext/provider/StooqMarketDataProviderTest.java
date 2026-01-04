package com.ai.livecontext.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.ai.livecontext.domain.MarketQuote;
import com.ai.livecontext.util.CorrelationIdHolder;
import java.math.BigDecimal;
import java.time.LocalDate;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

class StooqMarketDataProviderTest {

  private MockWebServer mockWebServer;
  private StooqMarketDataProvider provider;

  @BeforeEach
  void setUp() throws Exception {
    mockWebServer = new MockWebServer();
    mockWebServer.start();

    provider = new StooqMarketDataProvider();
    // Override the WebClient to point to our mock server
    WebClient webClient = WebClient.builder().baseUrl(mockWebServer.url("/").toString()).build();
    ReflectionTestUtils.setField(provider, "webClient", webClient);
  }

  @AfterEach
  void tearDown() throws Exception {
    mockWebServer.shutdown();
    CorrelationIdHolder.clear();
  }

  @Test
  void testProviderCreation() {
    assertNotNull(provider);
  }

  @Test
  void shouldFetchQuoteSuccessfully() throws InterruptedException {
    // Arrange
    String csvResponse =
        """
        Date,Open,High,Low,Close,Volume
        2024-01-15,150.00,155.00,149.50,154.25,1000000
        2024-01-14,148.00,151.00,147.00,150.00,950000
        """;

    mockWebServer.enqueue(
        new MockResponse().setBody(csvResponse).addHeader("Content-Type", "text/csv"));

    CorrelationIdHolder.set("test-correlation-123");

    // Act & Assert
    StepVerifier.create(provider.getQuote("AAPL.US", "daily"))
        .assertNext(
            quotes -> {
              assertThat(quotes).hasSize(2);

              MarketQuote quote1 = quotes.get(0);
              assertThat(quote1.getSymbol()).isEqualTo("AAPL.US");
              assertThat(quote1.getDate()).isEqualTo(LocalDate.of(2024, 1, 15));
              assertThat(quote1.getOpen()).isEqualTo(new BigDecimal("150.00"));
              assertThat(quote1.getHigh()).isEqualTo(new BigDecimal("155.00"));
              assertThat(quote1.getLow()).isEqualTo(new BigDecimal("149.50"));
              assertThat(quote1.getClose()).isEqualTo(new BigDecimal("154.25"));
              assertThat(quote1.getVolume()).isEqualTo(1000000L);

              MarketQuote quote2 = quotes.get(1);
              assertThat(quote2.getSymbol()).isEqualTo("AAPL.US");
              assertThat(quote2.getDate()).isEqualTo(LocalDate.of(2024, 1, 14));
            })
        .verifyComplete();

    // Verify request was made correctly
    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getPath()).contains("s=aapl.us");
    assertThat(request.getPath()).contains("i=d");
  }

  @Test
  void shouldNormalizeSymbolToLowercase() throws InterruptedException {
    String csvResponse = "Date,Open,High,Low,Close,Volume\n";
    mockWebServer.enqueue(new MockResponse().setBody(csvResponse));

    StepVerifier.create(provider.getQuote("TSLA.US", "daily"))
        .assertNext(quotes -> assertThat(quotes).isEmpty())
        .verifyComplete();

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getPath()).contains("s=tsla.us");
  }

  @Test
  void shouldMapDailyInterval() throws InterruptedException {
    String csvResponse = "Date,Open,High,Low,Close,Volume\n";
    mockWebServer.enqueue(new MockResponse().setBody(csvResponse));

    StepVerifier.create(provider.getQuote("aapl.us", "daily"))
        .assertNext(quotes -> assertThat(quotes).isEmpty())
        .verifyComplete();

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getPath()).contains("i=d");
  }

  @Test
  void shouldMapWeeklyInterval() throws InterruptedException {
    String csvResponse = "Date,Open,High,Low,Close,Volume\n";
    mockWebServer.enqueue(new MockResponse().setBody(csvResponse));

    StepVerifier.create(provider.getQuote("aapl.us", "weekly"))
        .assertNext(quotes -> assertThat(quotes).isEmpty())
        .verifyComplete();

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getPath()).contains("i=w");
  }

  @Test
  void shouldMapMonthlyInterval() throws InterruptedException {
    String csvResponse = "Date,Open,High,Low,Close,Volume\n";
    mockWebServer.enqueue(new MockResponse().setBody(csvResponse));

    StepVerifier.create(provider.getQuote("aapl.us", "monthly"))
        .assertNext(quotes -> assertThat(quotes).isEmpty())
        .verifyComplete();

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getPath()).contains("i=m");
  }

  @Test
  void shouldMapShortIntervalCodes() throws InterruptedException {
    String csvResponse = "Date,Open,High,Low,Close,Volume\n";
    mockWebServer.enqueue(new MockResponse().setBody(csvResponse));

    StepVerifier.create(provider.getQuote("aapl.us", "w"))
        .assertNext(quotes -> assertThat(quotes).isEmpty())
        .verifyComplete();

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getPath()).contains("i=w");
  }

  @Test
  void shouldDefaultToDailyForUnknownInterval() throws InterruptedException {
    String csvResponse = "Date,Open,High,Low,Close,Volume\n";
    mockWebServer.enqueue(new MockResponse().setBody(csvResponse));

    StepVerifier.create(provider.getQuote("aapl.us", "unknown"))
        .assertNext(quotes -> assertThat(quotes).isEmpty())
        .verifyComplete();

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getPath()).contains("i=d");
  }

  @Test
  void shouldHandleEmptyCSVResponse() {
    String csvResponse = "Date,Open,High,Low,Close,Volume\n";
    mockWebServer.enqueue(new MockResponse().setBody(csvResponse));

    StepVerifier.create(provider.getQuote("aapl.us", "daily"))
        .assertNext(quotes -> assertThat(quotes).isEmpty())
        .verifyComplete();
  }

  @Test
  void shouldHandleNetworkError() {
    mockWebServer.enqueue(new MockResponse().setResponseCode(500));

    StepVerifier.create(provider.getQuote("aapl.us", "daily")).expectError().verify();
  }

  @Test
  void shouldHandleConnectionTimeout() throws Exception {
    // Don't enqueue any response - will cause timeout/connection refused
    mockWebServer.shutdown();

    StepVerifier.create(provider.getQuote("aapl.us", "daily")).expectError().verify();
  }

  @Test
  void shouldParseCsvCorrectly() {
    String csvResponse =
        """
        Date,Open,High,Low,Close,Volume
        2024-12-01,100.50,102.75,99.25,101.00,5000000
        """;

    mockWebServer.enqueue(new MockResponse().setBody(csvResponse));

    StepVerifier.create(provider.getQuote("TEST.US", "daily"))
        .assertNext(
            quotes -> {
              assertThat(quotes).hasSize(1);
              MarketQuote quote = quotes.get(0);
              assertThat(quote.getSymbol()).isEqualTo("TEST.US");
              assertThat(quote.getDate()).isEqualTo(LocalDate.of(2024, 12, 1));
              assertThat(quote.getOpen()).isEqualByComparingTo("100.50");
              assertThat(quote.getHigh()).isEqualByComparingTo("102.75");
              assertThat(quote.getLow()).isEqualByComparingTo("99.25");
              assertThat(quote.getClose()).isEqualByComparingTo("101.00");
              assertThat(quote.getVolume()).isEqualTo(5000000L);
            })
        .verifyComplete();
  }

  @Test
  void shouldHandleMalformedCSV() {
    String csvResponse =
        """
        Date,Open,High,Low,Close,Volume
        invalid-date,not-a-number,100,100,100,100
        """;

    mockWebServer.enqueue(new MockResponse().setBody(csvResponse));

    StepVerifier.create(provider.getQuote("aapl.us", "daily"))
        .expectError(RuntimeException.class)
        .verify();
  }

  @Test
  void shouldHandleMissingCSVColumns() {
    String csvResponse =
        """
        Date,Open,High
        2024-01-01,100,101
        """;

    mockWebServer.enqueue(new MockResponse().setBody(csvResponse));

    StepVerifier.create(provider.getQuote("aapl.us", "daily"))
        .expectError(RuntimeException.class)
        .verify();
  }

  @Test
  void shouldPropagateCorrelationId() throws InterruptedException {
    String correlationId = "custom-correlation-xyz";
    CorrelationIdHolder.set(correlationId);

    String csvResponse = "Date,Open,High,Low,Close,Volume\n";
    mockWebServer.enqueue(new MockResponse().setBody(csvResponse));

    StepVerifier.create(provider.getQuote("aapl.us", "daily"))
        .assertNext(quotes -> assertThat(quotes).isEmpty())
        .verifyComplete();

    // Verify the correlation ID was used in logging (would need log capturing to fully test)
    // This test ensures no exceptions are thrown when correlation ID is set
    assertThat(CorrelationIdHolder.get()).isEqualTo(correlationId);
  }

  @Test
  void shouldHandleMultipleQuotes() {
    String csvResponse =
        """
        Date,Open,High,Low,Close,Volume
        2024-01-03,153.00,155.00,152.50,154.00,1100000
        2024-01-02,151.00,153.00,150.50,152.00,1050000
        2024-01-01,150.00,152.00,149.00,151.00,1000000
        """;

    mockWebServer.enqueue(new MockResponse().setBody(csvResponse));

    StepVerifier.create(provider.getQuote("AAPL.US", "daily"))
        .assertNext(
            quotes -> {
              assertThat(quotes).hasSize(3);
              assertThat(quotes.get(0).getDate()).isEqualTo(LocalDate.of(2024, 1, 3));
              assertThat(quotes.get(1).getDate()).isEqualTo(LocalDate.of(2024, 1, 2));
              assertThat(quotes.get(2).getDate()).isEqualTo(LocalDate.of(2024, 1, 1));
            })
        .verifyComplete();
  }

  @Test
  void shouldHandleCaseInsensitiveIntervalMapping() throws InterruptedException {
    String csvResponse = "Date,Open,High,Low,Close,Volume\n";
    mockWebServer.enqueue(new MockResponse().setBody(csvResponse));

    StepVerifier.create(provider.getQuote("aapl.us", "DAILY"))
        .assertNext(quotes -> assertThat(quotes).isEmpty())
        .verifyComplete();

    RecordedRequest request = mockWebServer.takeRequest();
    assertThat(request.getPath()).contains("i=d");
  }

  @Test
  void shouldHandleDecimalPrices() {
    String csvResponse =
        """
        Date,Open,High,Low,Close,Volume
        2024-01-01,99.99,100.01,99.98,100.00,500
        """;

    mockWebServer.enqueue(new MockResponse().setBody(csvResponse));

    StepVerifier.create(provider.getQuote("test.us", "daily"))
        .assertNext(
            quotes -> {
              assertThat(quotes).hasSize(1);
              MarketQuote quote = quotes.get(0);
              assertThat(quote.getOpen()).isEqualByComparingTo("99.99");
              assertThat(quote.getHigh()).isEqualByComparingTo("100.01");
              assertThat(quote.getLow()).isEqualByComparingTo("99.98");
              assertThat(quote.getClose()).isEqualByComparingTo("100.00");
            })
        .verifyComplete();
  }
}
