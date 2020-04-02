package com.newrelic.telemetry;

import com.newrelic.telemetry.spans.SpanBatchSender;
import com.newrelic.telemetry.spans.SpanBatchSenderBuilder;
import java.time.Duration;

public class SimpleSpanBatchSender {
  public static SpanBatchSender build(String apiKey) {
    return build(apiKey, Duration.ofSeconds(2));
  }

  public static SpanBatchSenderBuilder builder(String apiKey) {
    return builder(apiKey, Duration.ofSeconds(2));
  }

  public static SpanBatchSender build(String apiKey, Duration callTimeout) {
    return builder(apiKey, callTimeout).build();
  }

  public static SpanBatchSenderBuilder builder(String apiKey, Duration callTimeout) {
    return SpanBatchSender.builder().apiKey(apiKey).httpPoster(new Java11HttpPoster(callTimeout));
  }
}
