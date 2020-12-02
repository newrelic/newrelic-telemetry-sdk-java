package com.newrelic.telemetry;

/** To handle notifications, an implementation of this interface must be provided. */
public interface NotificationHandler {

  /**
   * Notice info events
   *
   * @param message the message provided by {@link TelemetryClient}
   * @param batch the telemetry batch that was sent
   */
  default void noticeInfo(String message, TelemetryBatch<? extends Telemetry> batch) {
    noticeInfo(message, null, batch);
  }

  /**
   * Notice info events
   *
   * @param message the message provided by {@link TelemetryClient}
   * @param exception the Exception provided by {@link TelemetryClient}
   * @param batch the telemetry batch that was sent
   */
  void noticeInfo(String message, Exception exception, TelemetryBatch<? extends Telemetry> batch);

  /**
   * Notice error events
   *
   * @param message the message provided by {@link TelemetryClient}
   * @param batch the telemetry batch that was sent
   */
  default void noticeError(String message, TelemetryBatch<? extends Telemetry> batch) {
    noticeError(message, null, batch);
  }

  /**
   * Notice info events
   *
   * @param message the message provided by {@link TelemetryClient}
   * @param throwable the throwable provided by {@link TelemetryClient}
   * @param batch the telemetry batch that was sent
   */
  void noticeError(String message, Throwable throwable, TelemetryBatch<? extends Telemetry> batch);
}
