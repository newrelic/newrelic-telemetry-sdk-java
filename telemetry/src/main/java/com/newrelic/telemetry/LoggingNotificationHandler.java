package com.newrelic.telemetry;

import org.slf4j.Logger;

/** The default NotificationHandler for logging {@link TelemetryClient} responses */
public class LoggingNotificationHandler implements NotificationHandler {

  private final Logger logger;

  LoggingNotificationHandler(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void noticeInfo(
      String message, Exception exception, TelemetryBatch<? extends Telemetry> batch) {
    logger.info(message, exception);
  }

  @Override
  public void noticeError(String message, Throwable t, TelemetryBatch<? extends Telemetry> batch) {
    logger.error(message, t);
  }
}
