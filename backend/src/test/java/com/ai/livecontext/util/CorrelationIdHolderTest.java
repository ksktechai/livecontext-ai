package com.ai.livecontext.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class CorrelationIdHolderTest {

  @AfterEach
  void tearDown() {
    CorrelationIdHolder.clear();
  }

  @Test
  void set_shouldStoreCorrelationId() {
    CorrelationIdHolder.set("test-correlation-id");
    assertEquals("test-correlation-id", CorrelationIdHolder.get());
  }

  @Test
  void get_shouldReturnNullWhenNotSet() {
    assertNull(CorrelationIdHolder.get());
  }

  @Test
  void clear_shouldRemoveCorrelationId() {
    CorrelationIdHolder.set("test-id");
    CorrelationIdHolder.clear();
    assertNull(CorrelationIdHolder.get());
  }

  @Test
  void get_shouldReturnDifferentValuesForDifferentThreads() throws InterruptedException {
    CorrelationIdHolder.set("main-thread-id");

    Thread anotherThread =
        new Thread(
            () -> {
              CorrelationIdHolder.set("another-thread-id");
              assertEquals("another-thread-id", CorrelationIdHolder.get());
            });

    anotherThread.start();
    anotherThread.join();

    // Main thread should still have its original value
    assertEquals("main-thread-id", CorrelationIdHolder.get());
  }
}
