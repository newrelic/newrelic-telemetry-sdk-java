package com.newrelic.telemetry;

import org.slf4j.Logger;

/**
 * The default NotificationHandler for logging {@link TelemetryClient} responses.
 *
 * <p>Users may choose to override this implementation to provide additional behavior. For example,
 * applications using the New Relic Java agent could report errors via the NewRelic.noticeError API.
 */
public class LoggingNotificationHandler implements NotificationHandler {

  private final Logger logger;

  LoggingNotificationHandler(Logger logger) {
    this.logger = logger;
  }

  @Override
  public void noticeInfo(
      String message, Exception exception, TelemetryBatch<? extends Telemetry> batch) {
    logger.info(addBatchType(message, batch), exception);
  }

  @Override
  public void noticeError(String message, Throwable t, TelemetryBatch<? extends Telemetry> batch) {
    logger.error(addBatchType(message, batch), t);
  }

  private String addBatchType(String message, TelemetryBatch<? extends Telemetry> batch) {
    if (batch != null) {
      return String.format("[%s] - %s", batch.getClass().getSimpleName(), message);
    }
    return message;
  }
}
