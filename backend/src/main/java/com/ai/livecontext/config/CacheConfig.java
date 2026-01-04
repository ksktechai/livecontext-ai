package com.ai.livecontext.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

  @Value("${livecontext.cache.quote-ttl-minutes:5}")
  private int quoteTtlMinutes;

  @Value("${livecontext.cache.quote-max-size:100}")
  private int quoteMaxSize;

  @Bean
  public CacheManager cacheManager() {
    CaffeineCacheManager cacheManager = new CaffeineCacheManager("marketQuotes");
    cacheManager.setCaffeine(
        Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(quoteTtlMinutes))
            .maximumSize(quoteMaxSize)
            .recordStats());
    return cacheManager;
  }
}
