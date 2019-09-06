package com.newrelic.telemetry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.spans.SpanBatchSender;
import com.newrelic.telemetry.spans.SpanBatchSenderBuilder;
import java.time.Duration;

public class SimpleSpanBatchSender {

  public static SpanBatchSender build(String apiKey) {
    return build(apiKey, Duration.ofSeconds(2));
  }

  public static SpanBatchSenderBuilder builder(String apiKey) {
    return builder(apiKey);
  }

  public static SpanBatchSender build(String apiKey, Duration callTimeout) {
    return builder(apiKey, callTimeout).build();
  }

  public static SpanBatchSenderBuilder builder(String apiKey, Duration callTimeout) {
    HttpPoster http = new OkHttpPoster(callTimeout);
    Gson gson = new GsonBuilder().create();
    return SpanBatchSender.builder()
        .apiKey(apiKey)
        .httpPoster(http)
        .attributesJson(new AttributesGson(gson));
  }
}
