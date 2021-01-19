/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core;

import com.newrelic.telemetry.core.SenderConfiguration.SenderConfigurationBuilder;
import com.newrelic.telemetry.core.http.HttpPoster;
import com.newrelic.telemetry.core.spans.SpanBatchSender;
import java.util.function.Supplier;

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
   * @param apiKey new relic api key
   * @return a new instance of a span batch sender
   */
  default SpanBatchSender createBatchSender(String apiKey) {
    SenderConfigurationBuilder configuration =
        SpanBatchSender.configurationBuilder().apiKey(apiKey).httpPoster(getPoster());
    return SpanBatchSender.create(configuration.build());
  }

  /**
   * Creates a new SenderConfigurationBuilder to help with constructing a SpanBatchSender. This
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
   * Create a new SpanBatchSenderBuilder with your New Relic Insights Insert API key.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   * @param apiKey new relic api key
   * @return a new instance of a span configuration builder
   */
  default SenderConfigurationBuilder configureWith(String apiKey) {
    return SpanBatchSender.configurationBuilder().apiKey(apiKey).httpPoster(getPoster());
  }

  HttpPoster getPoster();

  /**
   * Create an {@link SpanBatchSenderFactory} with an HTTP implementation.
   *
   * @param creator A {@link Supplier} that returns an {@link HttpPoster} implementation.
   * @return A Factory configured for use.
   */
  static SpanBatchSenderFactory fromHttpImplementation(Supplier<HttpPoster> creator) {
    return creator::get;
  }
}
