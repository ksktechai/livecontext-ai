package com.ai.livecontext.util;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppLogger {

  private static final Logger logger = LoggerFactory.getLogger(AppLogger.class);

  public static void log(String level, String eventType, String message, Map<String, Object> data) {
    StringBuilder logBuilder = new StringBuilder();
    logBuilder.append("[").append(eventType).append("] ").append(message);

    if (data != null && !data.isEmpty()) {
      logBuilder.append(" | ");
      data.forEach((key, value) -> logBuilder.append(key).append("=").append(value).append(" "));
    }

    String logMessage = logBuilder.toString().trim();

    switch (level.toUpperCase()) {
      case "DEBUG":
        logger.debug(logMessage);
        break;
      case "INFO":
        logger.info(logMessage);
        break;
      case "WARN":
        logger.warn(logMessage);
        break;
      case "ERROR":
        logger.error(logMessage);
        break;
      default:
        logger.info(logMessage);
    }
  }
}
