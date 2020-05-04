/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import com.newrelic.telemetry.SenderConfiguration.SenderConfigurationBuilder;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.spans.SpanBatchSender;
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
   */
  default SpanBatchSender createBatchSender(String apiKey) {
    SenderConfigurationBuilder configuration =
        SpanBatchSender.configurationBuilder().apiKey(apiKey).httpPoster(getPoster());
    return SpanBatchSender.create(configuration.build());
  }

  /**
   * Create a new SpanBatchSenderBuilder with your New Relic Insights Insert API key.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
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
