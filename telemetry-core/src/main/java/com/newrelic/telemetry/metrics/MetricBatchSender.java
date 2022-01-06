/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.BaseConfig;
import com.newrelic.telemetry.MetricBatchSenderFactory;
import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.SenderConfiguration;
import com.newrelic.telemetry.SenderConfiguration.SenderConfigurationBuilder;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.metrics.json.MetricBatchJsonCommonBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricBatchJsonTelemetryBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricBatchMarshaller;
import com.newrelic.telemetry.metrics.json.MetricToJson;
import com.newrelic.telemetry.transport.BatchDataSender;
import com.newrelic.telemetry.util.Utils;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Manages the sending of {@link MetricBatch} instances to the New Relic Metrics API. */
public class MetricBatchSender {

  private static final String METRICS_PATH = "/metric/v1";
  private static final String DEFAULT_URL = "https://metric-api.newrelic.com";
  private static final String EUROPEAN_URL = "https://metric-api.eu.newrelic.com";

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
   * Send batches of metrics to New Relic.
   *
   * @param metricBatches An ArrayList where each element is an MetricBatch. Each MetricBatch will
   *     be sent to New Relic one by one. Each batch will be drained of accumulated events as a part
   *     of this process.
   * @return The response from the ingest API.
   * @throws ResponseException In cases where the batch is unable to be successfully sent, one of
   *     the subclasses of {@link ResponseException} will be thrown. See the documentation on that
   *     hierarchy for details on the recommended ways to respond to those exceptions.
   */
  public Response sendBatch(ArrayList<MetricBatch> metricBatches) throws ResponseException {
    Response response = new Response(200, "success", "sent data");
    for (MetricBatch batch : metricBatches) {
      response = sendBatch(batch);

      // If there is an issue with a batch, stop sending batches
      if ((response.getStatusCode() != 200) && (response.getStatusCode() != 202)) {
        return response;
      }
    }
    return response;
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

    String userRegion = configuration.getRegion();

    String defaultUrl = DEFAULT_URL + METRICS_PATH;
    String endpointUrlToString = configuration.getEndpointUrl().toString();

    URL url = null;
    if (!endpointUrlToString.equals(defaultUrl)) {
      url = configuration.getEndpointUrl();
    } else {
      try {
        url = returnEndpoint(userRegion);
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }

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
            configuration.getSecondaryUserAgent(),
            configuration.useLicenseKey());

    return new MetricBatchSender(marshaller, sender);
  }

  public static URL returnEndpoint(String userRegion) throws MalformedURLException {
    URL url = null;
    if (userRegion.equals("US")) {
      try {
        url = new URL(DEFAULT_URL + METRICS_PATH);
        return url;
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    } else if (userRegion.equals("EU")) {
      try {
        url = new URL(EUROPEAN_URL + METRICS_PATH);
        return url;
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }
    throw new MalformedURLException(
        "A valid region (EU or US) needs to be added to generate the right endpoint");
  }

  public static SenderConfigurationBuilder configurationBuilder() {
    return SenderConfiguration.builder(DEFAULT_URL, METRICS_PATH);
  }
}
