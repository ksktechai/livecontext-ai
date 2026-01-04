package com.ai.livecontext.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class AppLoggerTest {

  @Test
  void log_shouldLogInfoMessage() {
    // Just verify no exception is thrown
    assertDoesNotThrow(() -> AppLogger.log("INFO", "test_event", "Test message", null));
  }

  @Test
  void log_shouldLogWithData() {
    Map<String, Object> data = new HashMap<>();
    data.put("key1", "value1");
    data.put("key2", 123);

    assertDoesNotThrow(() -> AppLogger.log("INFO", "test_event", "Test message", data));
  }

  @Test
  void log_shouldLogDebugLevel() {
    assertDoesNotThrow(() -> AppLogger.log("DEBUG", "debug_event", "Debug message", null));
  }

  @Test
  void log_shouldLogWarnLevel() {
    assertDoesNotThrow(() -> AppLogger.log("WARN", "warn_event", "Warning message", null));
  }

  @Test
  void log_shouldLogErrorLevel() {
    assertDoesNotThrow(() -> AppLogger.log("ERROR", "error_event", "Error message", null));
  }

  @Test
  void log_shouldDefaultToInfoForUnknownLevel() {
    assertDoesNotThrow(
        () -> AppLogger.log("UNKNOWN", "unknown_event", "Unknown level message", null));
  }

  @Test
  void log_shouldHandleEmptyData() {
    Map<String, Object> emptyData = new HashMap<>();

    assertDoesNotThrow(
        () -> AppLogger.log("INFO", "empty_data_event", "Empty data message", emptyData));
  }
}
