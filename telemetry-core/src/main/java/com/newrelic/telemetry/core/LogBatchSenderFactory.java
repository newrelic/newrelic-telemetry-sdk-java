/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core;

import com.newrelic.telemetry.core.SenderConfiguration.SenderConfigurationBuilder;
import com.newrelic.telemetry.core.http.HttpPoster;
import com.newrelic.telemetry.core.logs.LogBatchSender;
import java.util.function.Supplier;

/**
 * A factory interface for creating a LogBatchSender.
 *
 * <p>Concrete implementations use different HTTP providers.
 */
public interface LogBatchSenderFactory {

  /**
   * Create a new LogBatchSender with your New Relic Insights Insert API key, and otherwise default
   * settings. (2 second timeout, audit logging off, with the default endpoint URL)
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   * @param apiKey new relic api key
   * @return a new log batch sender instance
   */
  default LogBatchSender createBatchSender(String apiKey) {
    SenderConfigurationBuilder configuration =
        LogBatchSender.configurationBuilder().apiKey(apiKey).httpPoster(getPoster());
    return LogBatchSender.create(configuration.build());
  }

  /**
   * Creates a new SenderConfigurationBuilder to help with constructing a LogBatchSender. This
   * builder is configured with data from the BaseConfig, including the apiKey, audit logging
   * dis/enabled, and secondary user agent (which may be null).
   *
   * @param config a BaseConfig with settings to apply to the new builder
   * @return a new SenderConfigurationBuilder with the config applied
   */
  default SenderConfigurationBuilder configureWith(BaseConfig config) {
    return configureWith(config.getApiKey())
        .auditLoggingEnabled(config.isAuditLoggingEnabled())
        .secondaryUserAgent(config.getSecondaryUserAgent())
        .httpPoster(getPoster());
  }

  /**
   * Create a new {@link SenderConfigurationBuilder} with your New Relic Insights Insert API key.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   * @param apiKey new relic api key
   * @return a new sender configuration builder instance
   */
  default SenderConfigurationBuilder configureWith(String apiKey) {
    return LogBatchSender.configurationBuilder().apiKey(apiKey).httpPoster(getPoster());
  }

  HttpPoster getPoster();

  /**
   * Create an {@link LogBatchSenderFactory} with an HTTP implementation.
   *
   * @param creator A {@link Supplier} that returns an {@link HttpPoster} implementation.
   * @return A Factory configured for use.
   */
  static LogBatchSenderFactory fromHttpImplementation(Supplier<HttpPoster> creator) {
    return creator::get;
  }
}
