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
   */
  default EventBatchSender createBatchSender(String apiKey) {
    SenderConfigurationBuilder configuration =
        EventBatchSender.configurationBuilder().apiKey(apiKey).httpPoster(getPoster());
    return EventBatchSender.create(configuration.build());
  }

  /**
   * Create a new {@link SenderConfigurationBuilder} with your New Relic Insights Insert API key.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
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
