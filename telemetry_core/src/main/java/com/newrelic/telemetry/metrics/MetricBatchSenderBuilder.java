/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.SenderConfiguration.SenderConfigurationBuilder;
import com.newrelic.telemetry.http.HttpPoster;
import java.net.MalformedURLException;
import java.net.URI;

/**
 * Note: This class is deprecated and will be removed in the next major version - you should move to
 * the factories in telemetry-core
 *
 * <p>To be removed in 0.8.0
 */
@Deprecated
public class MetricBatchSenderBuilder {
  private final SenderConfigurationBuilder configBuilder;

  public MetricBatchSenderBuilder(SenderConfigurationBuilder senderConfigurationBuilder) {
    configBuilder = senderConfigurationBuilder;
  }

  /**
   * Build the final {@link MetricBatchSender}.
   *
   * @return the fully configured MetricBatchSender object
   */
  public MetricBatchSender build() {
    return MetricBatchSender.create(configBuilder.build());
  }

  /**
   * Set a URI to override the default ingest endpoint.
   *
   * @param uriOverride The scheme, host, and port that should be used for the Metrics API endpoint.
   *     The path component of this parameter is unused.
   * @return the Builder
   * @throws MalformedURLException This is thrown when the provided URI is malformed.
   */
  public MetricBatchSenderBuilder uriOverride(URI uriOverride) throws MalformedURLException {
    configBuilder.endpointWithPath(configBuilder.constructUrlWithHost(uriOverride));
    return this;
  }

  /**
   * Turns on audit logging. Payloads sent will be logged at the DEBUG level. Please note that if
   * your payloads contain sensitive information, that information will be logged wherever your logs
   * are configured.
   *
   * @return this builder
   */
  public MetricBatchSenderBuilder enableAuditLogging() {
    configBuilder.auditLoggingEnabled(true);
    return this;
  }

  /**
   * Provide your New Relic Insights Insert API key
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   * @param apiKey new relic api key
   * @return this builder
   */
  public MetricBatchSenderBuilder apiKey(String apiKey) {
    configBuilder.apiKey(apiKey);
    return this;
  }

  /**
   * Provide an implementation for HTTP POST. {@link #build()} will throw if an implementation is
   * not provided or this method is not called.
   *
   * @param httpPoster client responsible to execute the http request
   * @return this builder
   */
  public MetricBatchSenderBuilder httpPoster(HttpPoster httpPoster) {
    configBuilder.httpPoster(httpPoster);
    return this;
  }

  /**
   * Provide additional user agent information. The product is required to be non-null and
   * non-empty. The version is optional, although highly recommended.
   *
   * @param product to be used in the secondary user agent
   * @param version to be used in the secondary user agent
   * @return this builder
   */
  public MetricBatchSenderBuilder secondaryUserAgent(String product, String version) {
    if (version == null || version.isEmpty()) {
      configBuilder.secondaryUserAgent(product);
    } else {
      configBuilder.secondaryUserAgent(product + "/" + version);
    }
    return this;
  }
}
