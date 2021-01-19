/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core.metrics;

import com.newrelic.telemetry.core.BaseConfig;
import com.newrelic.telemetry.core.MetricBatchSenderFactory;
import com.newrelic.telemetry.core.Response;
import com.newrelic.telemetry.core.SenderConfiguration;
import com.newrelic.telemetry.core.SenderConfiguration.SenderConfigurationBuilder;
import com.newrelic.telemetry.core.exceptions.ResponseException;
import com.newrelic.telemetry.core.http.HttpPoster;
import com.newrelic.telemetry.core.json.AttributesJson;
import com.newrelic.telemetry.core.metrics.json.MetricBatchJsonCommonBlockWriter;
import com.newrelic.telemetry.core.metrics.json.MetricBatchJsonTelemetryBlockWriter;
import com.newrelic.telemetry.core.metrics.json.MetricBatchMarshaller;
import com.newrelic.telemetry.core.metrics.json.MetricToJson;
import com.newrelic.telemetry.core.transport.BatchDataSender;
import com.newrelic.telemetry.core.util.Utils;
import java.net.URL;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    return sender.send(json, batch);
  }

  /**
   * Creates a new MetricBatchSender with the given supplier of HttpPoster impl and a BaseConfig
   * instance, with all configuration NOT in BaseConfig being default.
   *
   * @param httpPosterCreator A supplier that returns an HttpPoster for this MetricBatchSender to
   *     use.
   * @param baseConfig basic configuration for the sender
   * @return a shiny new MetricBatchSender instance
   */
  public static MetricBatchSender create(
      Supplier<HttpPoster> httpPosterCreator, BaseConfig baseConfig) {
    return create(
        MetricBatchSenderFactory.fromHttpImplementation(httpPosterCreator)
            .configureWith(baseConfig)
            .build());
  }

  /**
   * Build the final {@link MetricBatchSender}.
   *
   * @param configuration new relict rest api ingest configurations
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
