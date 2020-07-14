/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.SenderConfiguration;
import com.newrelic.telemetry.SenderConfiguration.SenderConfigurationBuilder;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.metrics.json.MetricBatchJsonCommonBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricBatchJsonTelemetryBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricBatchMarshaller;
import com.newrelic.telemetry.metrics.json.MetricToJson;
import com.newrelic.telemetry.transport.BatchDataSender;
import com.newrelic.telemetry.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

/** Manages the sending of {@link MetricBatch} instances to the New Relic Metrics API. */
public class MetricBatchSender {

  private static final String METRICS_PATH = "/metric/v1";
  private static final String DEFAULT_URL = "https://metric-api.newrelic.com/";

  private static final Logger logger = LoggerFactory.getLogger(MetricBatchSender.class);

  private final MetricBatchMarshaller marshaller;
  private final BatchDataSender sender;

  MetricBatchSender(MetricBatchMarshaller marshaller, BatchDataSender sender) {
    this.marshaller = marshaller;
    this.sender = sender;
  }

  /**
   * Creates a builder for {@link MetricBatchSender}
   *
   * To be removed in 0.8.0
   *
   * @deprecated Use the {@link #create(SenderConfiguration)} method instead.
   */
  @Deprecated
  public static MetricBatchSenderBuilder builder() {
    return new MetricBatchSenderBuilder(MetricBatchSender.configurationBuilder());
  }

  /**
   * Send a batch of metrics to New Relic.
   *
   * @param batch The batch to send. This batch will be drained of accumulated metrics as a part of
   *     this process.
   * @return The response from the ingest API.
   * @throws ResponseException In cases where the batch is unable to be successfully sent, one of
   *     the subclasses of {@link ResponseException} will be thrown. See the documentation on that
   *     hierarchy for details on the recommended ways to respond to those exceptions.
   */
  public Response sendBatch(MetricBatch batch) throws ResponseException {
    if (batch == null || batch.size() == 0) {
      logger.debug("Skipped sending of an empty metric batch.");
      return new Response(202, "Ignored", "Empty batch");
    }
    logger.debug(
        "Sending a metric batch (number of metrics: {}) to the New Relic metric ingest endpoint)",
        batch.size());
    String json = marshaller.toJson(batch);
    return sender.send(json);
  }

  /**
   * Build the final {@link MetricBatchSender}.
   *
   * @return the fully configured MetricBatchSender object
   */
  public static MetricBatchSender create(SenderConfiguration configuration) {
    Utils.verifyNonNull(configuration.getApiKey(), "API key cannot be null");
    Utils.verifyNonNull(configuration.getHttpPoster(), "an HttpPoster implementation is required.");

    URL url = configuration.getEndpointUrl();

    MetricBatchMarshaller marshaller =
        new MetricBatchMarshaller(
            new MetricBatchJsonCommonBlockWriter(new AttributesJson()),
            new MetricBatchJsonTelemetryBlockWriter(new MetricToJson()));
    BatchDataSender sender =
        new BatchDataSender(
            configuration.getHttpPoster(),
            configuration.getApiKey(),
            url,
            configuration.isAuditLoggingEnabled(),
            configuration.getSecondaryUserAgent());

    return new MetricBatchSender(marshaller, sender);
  }

  public static SenderConfigurationBuilder configurationBuilder() {
    return SenderConfiguration.builder(DEFAULT_URL, METRICS_PATH);
  }
}
