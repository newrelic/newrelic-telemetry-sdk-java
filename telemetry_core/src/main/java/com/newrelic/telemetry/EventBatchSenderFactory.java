/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import com.newrelic.telemetry.events.EventBatchSender;
import com.newrelic.telemetry.events.EventBatchSenderBuilder;
import com.newrelic.telemetry.http.HttpPoster;
import java.time.Duration;
import java.util.function.Function;

/**
 * A factory interface for creating a EventBatchSender.
 *
 * <p>Concrete implementations use different HTTP providers.
 */
public interface EventBatchSenderFactory {

  /**
   * Create a new EventBatchSender with your New Relic Insights Insert API key, and otherwise
   * default settings. (2 second timeout, audit logging off, with the default endpoint URL)
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  default EventBatchSender build(String apiKey) {
    return builder(apiKey).build();
  }

  /**
   * Create a new EventBatchSenderBuilder with your New Relic Insights Insert API key.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  default EventBatchSenderBuilder builder(String apiKey) {
    return builder(apiKey, Duration.ofSeconds(2));
  }

  /**
   * Create a new EventBatchSender with your New Relic Insights Insert API key and a custom http
   * timeout; Otherwise default settings. (audit logging off and with the default endpoint URL)
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  default EventBatchSender build(String apiKey, Duration callTimeout) {
    return builder(apiKey, callTimeout).build();
  }

  /**
   * Create a new EventBatchSenderBuilder with your New Relic Insights Insert API key and a custom
   * http timeout.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  default EventBatchSenderBuilder builder(String apiKey, Duration callTimeout) {
    return EventBatchSender.builder().apiKey(apiKey).httpPoster(getPoster(callTimeout));
  }

  HttpPoster getPoster(Duration callTimeout);

  static EventBatchSenderFactory ofSender(Function<Duration, HttpPoster> lambda) {
    return duration -> lambda.apply(duration);
  }
}
