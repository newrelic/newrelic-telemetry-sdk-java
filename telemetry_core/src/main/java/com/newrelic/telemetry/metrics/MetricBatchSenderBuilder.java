/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.AbstractSenderBuilder;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.metrics.json.MetricBatchJsonCommonBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricBatchJsonTelemetryBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricBatchMarshaller;
import com.newrelic.telemetry.metrics.json.MetricToJson;
import com.newrelic.telemetry.transport.BatchDataSender;
import com.newrelic.telemetry.util.Utils;
import java.net.URL;

public class MetricBatchSenderBuilder
    extends AbstractSenderBuilder<MetricBatchSenderBuilder, MetricBatchSender> {

  private static final String metricsPath = "/metric/v1";
  private static final String DEFAULT_URL = "https://trace-api.newrelic.com/";

  /**
   * Build the final {@link MetricBatchSender}.
   *
   * @return the fully configured MetricBatchSender object
   */
  public MetricBatchSender build() {
    Utils.verifyNonNull(apiKey, "API key cannot be null");
    Utils.verifyNonNull(httpPoster, "an HttpPoster implementation is required.");

    URL url = getOrDefaultSendUrl();

    MetricBatchMarshaller marshaller =
        new MetricBatchMarshaller(
            new MetricBatchJsonCommonBlockWriter(new AttributesJson()),
            new MetricBatchJsonTelemetryBlockWriter(new MetricToJson()));
    BatchDataSender sender =
        new BatchDataSender(httpPoster, apiKey, url, auditLoggingEnabled, secondaryUserAgent);

    return new MetricBatchSender(marshaller, sender);
  }

  @Override
  protected String getDefaultUrl() {
    return DEFAULT_URL;
  }

  @Override
  protected String getBasePath() {
    return metricsPath;
  }
}
