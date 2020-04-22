/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.AbstractSenderBuilder;
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

public class SpanBatchSenderBuilder
    extends AbstractSenderBuilder<SpanBatchSenderBuilder, SpanBatchSender> {

  private static final String spansPath = "/trace/v1";
  private static final String DEFAULT_URL = "https://trace-api.newrelic.com/";

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
        new BatchDataSender(httpPoster, apiKey, traceUrl, auditLoggingEnabled, secondaryUserAgent);
    return new SpanBatchSender(marshaller, sender);
  }

  private URL getOrDefaultTraceUrl() {
    if (sendUrl != null) {
      return sendUrl;
    }
    try {
      return constructUrlWithHost(URI.create(DEFAULT_URL));
    } catch (MalformedURLException e) {
      throw new UncheckedIOException("Bad hardcoded URL", e);
    }
  }

  @Override
  protected String getDefaultUrl() {
    return DEFAULT_URL;
  }

  @Override
  protected String getBasePath() {
    return spansPath;
  }
}
