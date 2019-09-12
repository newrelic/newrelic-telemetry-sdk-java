/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.metrics.json.MetricBatchMarshaller;
import com.newrelic.telemetry.transport.BatchDataSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Manages the sending of {@link MetricBatch} instances to the New Relic Metrics API. */
public class MetricBatchSender {

  private static final Logger logger = LoggerFactory.getLogger(MetricBatchSender.class);

  private final MetricBatchMarshaller marshaller;
  private final BatchDataSender sender;

  MetricBatchSender(MetricBatchMarshaller marshaller, BatchDataSender sender) {
    this.marshaller = marshaller;
    this.sender = sender;
  }

  public static MetricBatchSenderBuilder builder() {
    return new MetricBatchSenderBuilder();
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
}
