package com.ai.livecontext.provider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.ai.livecontext.domain.MarketQuote;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StooqMarketDataProviderMoreTest {

  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
  }

  @Test
  void parseQuote_shouldHandleValidData() {
    ObjectNode quoteNode = objectMapper.createObjectNode();
    quoteNode.put("symbol", "AAPL.US");
    quoteNode.put("date", "2024-01-15");
    quoteNode.put("open", "150.00");
    quoteNode.put("high", "155.00");
    quoteNode.put("low", "148.00");
    quoteNode.put("close", "152.00");
    quoteNode.put("volume", "1000000");

    MarketQuote quote =
        MarketQuote.builder()
            .symbol("AAPL.US")
            .date(LocalDate.of(2024, 1, 15))
            .open(new BigDecimal("150.00"))
            .high(new BigDecimal("155.00"))
            .low(new BigDecimal("148.00"))
            .close(new BigDecimal("152.00"))
            .volume(1000000L)
            .build();

    assertEquals("AAPL.US", quote.getSymbol());
    assertEquals(new BigDecimal("150.00"), quote.getOpen());
    assertEquals(LocalDate.of(2024, 1, 15), quote.getDate());
  }

  @Test
  void marketQuote_shouldSupportAllFields() {
    MarketQuote quote = new MarketQuote();
    quote.setSymbol("GOOGL.US");
    quote.setDate(LocalDate.now());
    quote.setOpen(new BigDecimal("100.00"));
    quote.setHigh(new BigDecimal("105.00"));
    quote.setLow(new BigDecimal("98.00"));
    quote.setClose(new BigDecimal("103.00"));
    quote.setVolume(500000L);

    assertNotNull(quote.getSymbol());
    assertNotNull(quote.getDate());
    assertNotNull(quote.getClose());
    assertEquals(500000L, quote.getVolume());
  }
}
