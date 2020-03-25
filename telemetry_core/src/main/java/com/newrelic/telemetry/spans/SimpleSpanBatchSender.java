/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.http.HttpPoster;
import java.time.Duration;
import java.util.function.Function;

/**
 * A builder class for creating a SpanBatchSender that uses okhttp as the underlying http client
 * implementation.
 *
 * <p>Note: This class will be deprecated in the near future and be replaced with something named
 * more succinctly.
 */
public class SimpleSpanBatchSender {

  private static Function<Duration, HttpPoster> ctor = null;

  public static void provider(Function<Duration, HttpPoster> provider) {
    ctor = provider;
  }

  /**
   * Create a new SpanBatchSender with your New Relic Insights Insert API key, and otherwise default
   * settings. (2 second timeout, audit logging off, with the default endpoint URL)
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  public static SpanBatchSender build(String apiKey) {
    return build(apiKey, Duration.ofSeconds(2));
  }

  /**
   * Create a new SpanBatchSenderBuilder with your New Relic Insights Insert API key.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  public static SpanBatchSenderBuilder builder(String apiKey) {
    return builder(apiKey, Duration.ofSeconds(2));
  }

  /**
   * Create a new SpanBatchSender with your New Relic Insights Insert API key and a custom http
   * timeout; Otherwise default settings. (audit logging off and with the default endpoint URL)
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  public static SpanBatchSender build(String apiKey, Duration callTimeout) {
    return builder(apiKey, callTimeout).build();
  }

  /**
   * Create a new SpanBatchSenderBuilder with your New Relic Insights Insert API key and a custom
   * http timeout.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  public static SpanBatchSenderBuilder builder(String apiKey, Duration callTimeout) {
    return SpanBatchSender.builder().apiKey(apiKey).httpPoster(ctor.apply(callTimeout));
  }
}
