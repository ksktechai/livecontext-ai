package com.ai.livecontext.provider;

import com.ai.livecontext.domain.MarketQuote;
import java.util.List;
import reactor.core.publisher.Mono;

public interface MarketDataProvider {

  Mono<List<MarketQuote>> getQuote(String symbol, String interval);
}
