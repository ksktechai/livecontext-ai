package com.ai.livecontext.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MarketQuoteTest {

  @Test
  void builder_shouldCreateMarketQuote() {
    MarketQuote quote =
        MarketQuote.builder()
            .symbol("AAPL")
            .open(new BigDecimal("150.00"))
            .high(new BigDecimal("155.00"))
            .low(new BigDecimal("148.00"))
            .close(new BigDecimal("153.00"))
            .volume(1000000L)
            .date(LocalDate.of(2024, 1, 15))
            .build();

    assertEquals("AAPL", quote.getSymbol());
    assertEquals(new BigDecimal("150.00"), quote.getOpen());
    assertEquals(new BigDecimal("155.00"), quote.getHigh());
    assertEquals(new BigDecimal("148.00"), quote.getLow());
    assertEquals(new BigDecimal("153.00"), quote.getClose());
    assertEquals(1000000L, quote.getVolume());
    assertEquals(LocalDate.of(2024, 1, 15), quote.getDate());
  }

  @Test
  void setters_shouldModifyFields() {
    MarketQuote quote = new MarketQuote();
    quote.setSymbol("GOOGL");
    quote.setClose(new BigDecimal("175.50"));
    quote.setVolume(500000L);

    assertEquals("GOOGL", quote.getSymbol());
    assertEquals(new BigDecimal("175.50"), quote.getClose());
    assertEquals(500000L, quote.getVolume());
  }

  @Test
  void noArgsConstructor_shouldWork() {
    MarketQuote quote = new MarketQuote();
    assertNull(quote.getSymbol());
    assertNull(quote.getClose());
    assertNull(quote.getVolume());
  }
}
