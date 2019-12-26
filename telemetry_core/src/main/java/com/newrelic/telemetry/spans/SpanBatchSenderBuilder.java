/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.spans.json.SpanBatchMarshaller;
import com.newrelic.telemetry.spans.json.SpanJsonCommonBlockWriter;
import com.newrelic.telemetry.spans.json.SpanJsonTelemetryBlockWriter;
import com.newrelic.telemetry.transport.BatchDataSender;
import com.newrelic.telemetry.util.Utils;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class SpanBatchSenderBuilder {

  private static final String spansPath = "/trace/v1";
  private static final String DEFAULT_TRACE_URL = "https://trace-api.newrelic.com/";

  private String apiKey;
  private HttpPoster httpPoster;

  private URL traceUrl;
  private boolean auditLoggingEnabled = false;

  private String additionalUserAgent;

  /**
   * Build the final {@link SpanBatchSender}.
   *
   * @return the fully configured SpanBatchSender object
   */
  public SpanBatchSender build() {
    Utils.verifyNonNull(apiKey, "API key cannot be null");
    Utils.verifyNonNull(httpPoster, "an HttpPoster implementation is required.");

    URL traceUrl = getOrDefaultTraceUrl();

    SpanBatchMarshaller marshaller =
        new SpanBatchMarshaller(
            new SpanJsonCommonBlockWriter(new AttributesJson()),
            new SpanJsonTelemetryBlockWriter(new AttributesJson()));
    BatchDataSender sender =
        new BatchDataSender(httpPoster, apiKey, traceUrl, auditLoggingEnabled, additionalUserAgent);
    return new SpanBatchSender(marshaller, sender);
  }

  private URL getOrDefaultTraceUrl() {
    if (traceUrl != null) {
      return traceUrl;
    }
    try {
      return constructSpansUrlWithHost(URI.create(DEFAULT_TRACE_URL));
    } catch (MalformedURLException e) {
      throw new UncheckedIOException("Bad hardcoded URL", e);
    }
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
    this.traceUrl = constructSpansUrlWithHost(uriOverride);
    return this;
  }

  /**
   * Turns on audit logging. Payloads sent will be logged at the DEBUG level. Please note that if
   * your payloads contain sensitive information, that information will be logged wherever your logs
   * are configured.
   */
  public SpanBatchSenderBuilder enableAuditLogging() {
    this.auditLoggingEnabled = true;
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
    this.apiKey = apiKey;
    return this;
  }

  /**
   * Provide an implementation for HTTP POST. {@link #build()} will throw if an implementation is
   * not provided or this method is not called.
   */
  public SpanBatchSenderBuilder httpPoster(HttpPoster httpPoster) {
    this.httpPoster = httpPoster;
    return this;
  }

  /**
   * By default, {@value #DEFAULT_TRACE_URL} is used. Otherwise uses the provided {@code traceUrl}
   *
   * @deprecated Use the {@link #uriOverride(URI)} method instead.
   */
  public SpanBatchSenderBuilder traceUrl(URL traceUrl) {
    this.traceUrl = traceUrl;
    return this;
  }

  /**
   * Provide additional user agent information. The product is required to be non-null and
   * non-empty. The version is optional, although highly recommended.
   */
  public SpanBatchSenderBuilder additionalUserAgent(String product, String version) {
    Utils.verifyNonNull(product, "Product cannot be null in the additional user-agent.");
    if (version == null || version.isEmpty()) {
      additionalUserAgent = product;
    } else {
      additionalUserAgent = product + "/" + version;
    }
    return this;
  }

  private static URL constructSpansUrlWithHost(URI hostUri) throws MalformedURLException {
    return hostUri.resolve(spansPath).toURL();
  }
}
