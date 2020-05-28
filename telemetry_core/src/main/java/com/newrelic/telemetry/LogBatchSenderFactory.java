/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import com.newrelic.telemetry.SenderConfiguration.SenderConfigurationBuilder;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.logs.LogBatchSender;
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
   */
  default LogBatchSender createBatchSender(String apiKey) {
    SenderConfigurationBuilder configuration =
        LogBatchSender.configurationBuilder().apiKey(apiKey).httpPoster(getPoster());
    return LogBatchSender.create(configuration.build());
  }

  /**
   * Create a new {@link SenderConfigurationBuilder} with your New Relic Insights Insert API key.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
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
