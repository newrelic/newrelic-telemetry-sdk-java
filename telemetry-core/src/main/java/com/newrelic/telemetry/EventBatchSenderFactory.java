/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import com.newrelic.telemetry.SenderConfiguration.SenderConfigurationBuilder;
import com.newrelic.telemetry.events.EventBatchSender;
import com.newrelic.telemetry.http.HttpPoster;
import java.util.function.Supplier;

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
   * @param apiKey new relic api key
   * @return a new event batch sender instance
   */
  default EventBatchSender createBatchSender(String apiKey) {
    SenderConfigurationBuilder configuration =
        EventBatchSender.configurationBuilder().apiKey(apiKey).httpPoster(getPoster());
    return EventBatchSender.create(configuration.build());
  }

  /**
   * Creates a new SenderConfigurationBuilder to help with constructing a EventBatchSender. This
   * builder is configured with data from the BaseConfig, including the apiKey, audit logging
   * dis/enabled, and secondary user agent (which may be null).
   *
   * @param baseConfig a BaseConfig with settings to apply to the new builder
   * @return a new SenderConfigurationBuilder with the config applied
   */
  default SenderConfigurationBuilder configureWith(BaseConfig baseConfig) {
    return configureWith(baseConfig.getApiKey())
        .secondaryUserAgent(baseConfig.getSecondaryUserAgent())
        .auditLoggingEnabled(baseConfig.isAuditLoggingEnabled());
  }

  /**
   * Create a new {@link SenderConfigurationBuilder} with your New Relic Insights Insert API key.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   * @param apiKey new relic api key
   * @return a new sender configuration instance
   */
  default SenderConfigurationBuilder configureWith(String apiKey) {
    return EventBatchSender.configurationBuilder().apiKey(apiKey).httpPoster(getPoster());
  }

  HttpPoster getPoster();

  /**
   * Create an {@link EventBatchSenderFactory} with an HTTP implementation.
   *
   * @param creator A {@link Supplier} that returns an {@link HttpPoster} implementation.
   * @return A Factory configured for use.
   */
  static EventBatchSenderFactory fromHttpImplementation(Supplier<HttpPoster> creator) {
    return creator::get;
  }
}
