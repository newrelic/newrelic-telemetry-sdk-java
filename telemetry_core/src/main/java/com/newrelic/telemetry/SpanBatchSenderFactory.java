/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static java.time.temporal.ChronoUnit.SECONDS;

import com.newrelic.telemetry.SenderConfiguration.SenderConfigurationBuilder;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.spans.SpanBatchSender;
import java.time.Duration;
import java.util.function.Function;

/**
 * A factory interface for creating a SpanBatchSender.
 *
 * <p>Concrete implementations use different HTTP providers.
 */
public interface SpanBatchSenderFactory {

  /**
   * Create a new SpanBatchSender with your New Relic Insights Insert API key, and otherwise default
   * settings. (2 second timeout, audit logging off, with the default endpoint URL)
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  default SpanBatchSender createBatchSender(String apiKey) {
    return createBatchSender(apiKey, Duration.ofSeconds(2));
  }

  /**
   * Create a new SpanBatchSender with your New Relic Insights Insert API key and a custom http
   * timeout; Otherwise default settings. (audit logging off and with the default endpoint URL)
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  default SpanBatchSender createBatchSender(String apiKey, Duration callTimeout) {
    return createBatchSender(apiKey, Duration.of(2, SECONDS));
  }

  /**
   * Create a new SpanBatchSenderBuilder with your New Relic Insights Insert API key.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  default SenderConfigurationBuilder configureWith(String apiKey) {
    return configureWith(apiKey, Duration.ofSeconds(2));
  }

  /**
   * Create a new SpanBatchSenderBuilder with your New Relic Insights Insert API key and a custom
   * http timeout.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  default SenderConfigurationBuilder configureWith(String apiKey, Duration callTimeout) {
    return SpanBatchSender.configurationBuilder().apiKey(apiKey).httpPoster(getPoster(callTimeout));
  }

  HttpPoster getPoster(Duration callTimeout);

  static SpanBatchSenderFactory fromHttpImplementation(Function<Duration, HttpPoster> lambda) {
    return duration -> lambda.apply(duration);
  }
}
