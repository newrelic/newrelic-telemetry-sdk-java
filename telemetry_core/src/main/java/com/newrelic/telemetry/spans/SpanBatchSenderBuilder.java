/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.SenderConfiguration.SenderConfigurationBuilder;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.util.Utils;
import java.net.MalformedURLException;
import java.net.URI;

/**
 * Note: This class is deprecated and will be removed in the next major version - you should move to
 * the factories in telemetry-core
 */
@Deprecated
public class SpanBatchSenderBuilder {
  private final SenderConfigurationBuilder configBuilder;

  public SpanBatchSenderBuilder(SenderConfigurationBuilder configurationBuilder) {
    configBuilder = configurationBuilder;
  }

  /**
   * Build the final {@link SpanBatchSender}.
   *
   * @return the fully configured SpanBatchSender object
   */
  public SpanBatchSender build() {
    return SpanBatchSender.create(configBuilder.build());
  }

  /**
   * Set a URI to override the default ingest endpoint.
   *
   * @param uriOverride The scheme, host, and port that should be used for the Spans API endpoint.
   *     The path component of this parameter is unused.
   * @return the Builder
   * @throws MalformedURLException This is thrown when the provided URI is malformed.
   */
  public SpanBatchSenderBuilder uriOverride(URI uriOverride) throws MalformedURLException {
    configBuilder.endpointWithPath(configBuilder.constructUrlWithHost(uriOverride));
    return this;
  }

  /**
   * Turns on audit logging. Payloads sent will be logged at the DEBUG level. Please note that if
   * your payloads contain sensitive information, that information will be logged wherever your logs
   * are configured.
   */
  public SpanBatchSenderBuilder enableAuditLogging() {
    configBuilder.auditLoggingEnabled(true);
    return this;
  }

  /**
   * Provide your New Relic Insights Insert API key
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  public SpanBatchSenderBuilder apiKey(String apiKey) {
    configBuilder.apiKey(apiKey);
    return this;
  }

  /**
   * Provide an implementation for HTTP POST. {@link #build()} will throw if an implementation is
   * not provided or this method is not called.
   */
  public SpanBatchSenderBuilder httpPoster(HttpPoster httpPoster) {
    configBuilder.httpPoster(httpPoster);
    return this;
  }

  /**
   * Provide additional user agent information. The product is required to be non-null and
   * non-empty. The version is optional, although highly recommended.
   */
  public SpanBatchSenderBuilder secondaryUserAgent(String product, String version) {
    Utils.verifyNonBlank(product, "Product cannot be null or empty in the secondary user-agent.");
    if (version == null || version.isEmpty()) {
      configBuilder.secondaryUserAgent(product);
    } else {
      configBuilder.secondaryUserAgent(product + "/" + version);
    }
    return this;
  }
}
