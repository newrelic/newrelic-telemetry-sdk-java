package com.newrelic.telemetry;

import com.newrelic.telemetry.metrics.MetricBatchSender;
import com.newrelic.telemetry.metrics.MetricBatchSenderBuilder;
import java.time.Duration;

public class SimpleMetricBatchSender {
  public static MetricBatchSender build(String apiKey) {
    return builder(apiKey).build();
  }

  public static MetricBatchSenderBuilder builder(String apiKey) {
    return builder(apiKey, Duration.ofSeconds(2));
  }

  public static MetricBatchSender build(String apiKey, Duration callTimeout) {
    return builder(apiKey, callTimeout).build();
  }

  public static MetricBatchSenderBuilder builder(String apiKey, Duration callTimeout) {
    return MetricBatchSender.builder().apiKey(apiKey).httpPoster(new Java11HttpPoster(callTimeout));
  }
}
