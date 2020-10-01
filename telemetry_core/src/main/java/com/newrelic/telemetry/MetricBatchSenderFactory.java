/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import com.newrelic.telemetry.SenderConfiguration.SenderConfigurationBuilder;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.metrics.MetricBatchSender;
import java.util.function.Supplier;

/**
 * A factory interface for creating a MetricBatchSender.
 *
 * <p>Concrete implementations use different HTTP providers.
 */
public interface MetricBatchSenderFactory {

  /**
   * Create a new MetricBatchSender with your New Relic Insights Insert API key, and otherwise
   * default settings. (2 second timeout, audit logging off, with the default endpoint URL)
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   * @param apiKey new relic api key
   * @return a new metric batch sender instance
   */
  default MetricBatchSender createBatchSender(String apiKey) {
    SenderConfigurationBuilder configuration =
        MetricBatchSender.configurationBuilder().apiKey(apiKey).httpPoster(getPoster());
    return MetricBatchSender.create(configuration.build());
  }

  /**
   * Creates a new SenderConfigurationBuilder to help with constructing a MetricBatchSender. This
   * builder is configured with data from the BaseConfig, including the apiKey, audit logging
   * dis/enabled, and secondary user agent (which may be null).
   *
   * @param baseConfig a BaseConfig with settings to apply to the new builder
   * @return a new SenderConfigurationBuilder with the config applied
   */
  default SenderConfigurationBuilder configureWith(BaseConfig baseConfig) {
    return configureWith(baseConfig.getApiKey())
        .auditLoggingEnabled(baseConfig.isAuditLoggingEnabled())
        .secondaryUserAgent(baseConfig.getSecondaryUserAgent());
  }

  /**
   * Create a new MetricBatchSenderBuilder with your New Relic Insights Insert API key.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   * @param apiKey new relic api key
   * @return a new sender configuration builder instance
   */
  default SenderConfigurationBuilder configureWith(String apiKey) {
    return MetricBatchSender.configurationBuilder().apiKey(apiKey).httpPoster(getPoster());
  }

  HttpPoster getPoster();

  /**
   * Create an {@link MetricBatchSenderFactory} with an HTTP implementation.
   *
   * @param creator A {@link Supplier} that returns an {@link HttpPoster} implementation.
   * @return A Factory configured for use.
   */
  static MetricBatchSenderFactory fromHttpImplementation(Supplier<HttpPoster> creator) {
    return creator::get;
  }
}
