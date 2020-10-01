/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import com.newrelic.telemetry.SenderConfiguration.SenderConfigurationBuilder;
import com.newrelic.telemetry.metrics.MetricBatchSender;
import com.newrelic.telemetry.metrics.MetricBatchSenderBuilder;
import java.time.Duration;

/**
 * A builder class for creating a MetricBatchSender that uses okhttp as the underlying http client
 * implementation.
 *
 * <p>To be removed in 0.8.0
 *
 * <p>Note: This class is deprecated and will be removed in the next major version - you should move
 * to the factories in telemetry-core
 */
@Deprecated
public class SimpleMetricBatchSender {

  /**
   * Create a new MetricBatchSender with your New Relic Insights Insert API key, and otherwise
   * default settings. (2 second timeout, audit logging off, with the default endpoint URL)
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   * @param apiKey new relic api key
   * @return a new instance of a metric batch sender
   */
  public static MetricBatchSender build(String apiKey) {
    return builder(apiKey).build();
  }

  /**
   * Create a new MetricBatchSenderBuilder with your New Relic Insights Insert API key.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   * @param apiKey new relic api key
   * @return a new instance of a metric batch sender builder
   */
  public static MetricBatchSenderBuilder builder(String apiKey) {
    return builder(apiKey, Duration.ofSeconds(2));
  }

  /**
   * Create a new MetricBatchSender with your New Relic Insights Insert API key and a custom http
   * timeout; Otherwise default settings. (audit logging off and with the default endpoint URL)
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   * @param apiKey new relic api key
   * @param callTimeout http connection timeout
   * @return a new instance of a metric batch sender
   */
  public static MetricBatchSender build(String apiKey, Duration callTimeout) {
    return builder(apiKey, callTimeout).build();
  }

  /**
   * Create a new MetricBatchSenderBuilder with your New Relic Insights Insert API key and a custom
   * http timeout.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   * @param apiKey new relic api key
   * @param callTimeout http connection timeout
   * @return a new instance of a metric batch sender builder
   */
  public static MetricBatchSenderBuilder builder(String apiKey, Duration callTimeout) {
    SenderConfigurationBuilder configurationBuilder =
        MetricBatchSender.configurationBuilder()
            .apiKey(apiKey)
            .httpPoster(new OkHttpPoster(callTimeout));
    return new MetricBatchSenderBuilder(configurationBuilder);
  }
}
