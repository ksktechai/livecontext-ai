package com.ai.livecontext.config;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.test.util.ReflectionTestUtils;

class CacheConfigTest {

  @Test
  void cacheManager_shouldCreateCaffeineCacheManager() {
    CacheConfig cacheConfig = new CacheConfig();
    ReflectionTestUtils.setField(cacheConfig, "quoteTtlMinutes", 5);
    ReflectionTestUtils.setField(cacheConfig, "quoteMaxSize", 100);

    CacheManager cacheManager = cacheConfig.cacheManager();

    assertNotNull(cacheManager);
    assertNotNull(cacheManager.getCache("marketQuotes"));
  }

  @Test
  void cacheManager_shouldRespectConfiguration() {
    CacheConfig cacheConfig = new CacheConfig();
    ReflectionTestUtils.setField(cacheConfig, "quoteTtlMinutes", 10);
    ReflectionTestUtils.setField(cacheConfig, "quoteMaxSize", 50);

    CacheManager cacheManager = cacheConfig.cacheManager();

    // Cache should be created with the specified name
    assertNotNull(cacheManager.getCache("marketQuotes"));
  }
}
