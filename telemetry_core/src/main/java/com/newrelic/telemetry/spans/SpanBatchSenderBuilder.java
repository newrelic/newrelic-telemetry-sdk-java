/*
 * ---------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
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

  private String apiKey;
  private HttpPoster httpPoster;

  private URL traceUrl;
  private boolean auditLoggingEnabled = false;

  /**
   * Build the final {@link SpanBatchSender}.
   *
   * @return the fully configured SpanBatchSender object
   */
  public SpanBatchSender build() {
    Utils.verifyNonNull(apiKey, "API key cannot be null");
    Utils.verifyNonNull(httpPoster, "an HttpPoster implementation is required.");
    Utils.verifyNonNull(attributesJson, "an AttributesJson implementation is required.");

    URL traceUrl = getOrDefaultTraceUrl();

    SpanBatchMarshaller marshaller =
        new SpanBatchMarshaller(
            new SpanJsonCommonBlockWriter(new AttributesJson()),
            new SpanJsonTelemetryBlockWriter(new AttributesJson()));
    BatchDataSender sender = new BatchDataSender(httpPoster, apiKey, traceUrl, auditLoggingEnabled);
    return new SpanBatchSender(marshaller, sender);
  }

  private URL getOrDefaultTraceUrl() {
    if (traceUrl == null) {
      try {
        return constructSpansUrlWithHost(URI.create("https://trace-api.newrelic.com/"));
      } catch (MalformedURLException e) {
        throw new UncheckedIOException("Bad hardcoded URL", e);
      }
    }
    return traceUrl;
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

  public SpanBatchSenderBuilder httpPoster(HttpPoster httpPoster) {
    this.httpPoster = httpPoster;
    return this;
  }

  public SpanBatchSenderBuilder traceUrl(URL traceUrl) {
    this.traceUrl = traceUrl;
    return this;
  }

  private static URL constructSpansUrlWithHost(URI hostUri) throws MalformedURLException {
    return hostUri.resolve(spansPath).toURL();
  }
}
