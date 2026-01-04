package com.ai.livecontext.util;

import org.slf4j.MDC;

public class CorrelationIdHolder {

  private static final String CORRELATION_ID_KEY = "correlationId";

  public static void set(String id) {
    MDC.put(CORRELATION_ID_KEY, id);
  }

  public static String get() {
    return MDC.get(CORRELATION_ID_KEY);
  }

  public static void clear() {
    MDC.remove(CORRELATION_ID_KEY);
  }
}
