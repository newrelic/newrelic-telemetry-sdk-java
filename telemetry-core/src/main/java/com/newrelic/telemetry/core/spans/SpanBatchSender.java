/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core.spans;

import com.newrelic.telemetry.core.BaseConfig;
import com.newrelic.telemetry.core.Response;
import com.newrelic.telemetry.core.SenderConfiguration;
import com.newrelic.telemetry.core.SpanBatchSenderFactory;
import com.newrelic.telemetry.core.exceptions.ResponseException;
import com.newrelic.telemetry.core.http.HttpPoster;
import com.newrelic.telemetry.core.json.AttributesJson;
import com.newrelic.telemetry.core.spans.json.SpanBatchMarshaller;
import com.newrelic.telemetry.core.spans.json.SpanJsonCommonBlockWriter;
import com.newrelic.telemetry.core.spans.json.SpanJsonTelemetryBlockWriter;
import com.newrelic.telemetry.core.transport.BatchDataSender;
import com.newrelic.telemetry.core.util.Utils;
import java.net.URL;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Manages the sending of {@link SpanBatch} instances to the New Relic Spans API. */
public class SpanBatchSender {

  private static final String SPANS_PATH = "/trace/v1";
  private static final String DEFAULT_URL = "https://trace-api.newrelic.com/";

  private static final Logger logger = LoggerFactory.getLogger(SpanBatchSender.class);

  private final SpanBatchMarshaller marshaller;
  private final BatchDataSender sender;

  /**
   * Creates a span batch sender that knows how to marshall span batches and send them
   *
   * @param marshaller Defines how to marshall span batches
   * @param sender Sends span batches
   */
  SpanBatchSender(SpanBatchMarshaller marshaller, BatchDataSender sender) {
    this.marshaller = marshaller;
    this.sender = sender;
  }

  /**
   * /** Send a batch of spans to New Relic.
   *
   * @param batch The batch to send. This batch will be drained of accumulated spans as a part of
   *     this process.
   * @return The response from the ingest API.
   * @throws ResponseException In cases where the batch is unable to be successfully sent, one of
   *     the subclasses of {@link ResponseException} will be thrown. See the documentation on that
   *     hierarchy for details on the recommended ways to respond to those exceptions.
   */
  public Response sendBatch(SpanBatch batch) throws ResponseException {
    if (batch == null || batch.size() == 0) {
      logger.debug("Skipped sending a null or empty span batch");
      return new Response(202, "Ignored", "Empty batch");
    }
    logger.debug(
        "Sending a span batch (number of spans: {}) to the New Relic span ingest endpoint)",
        batch.size());
    String json = marshaller.toJson(batch);
    return sender.send(json, batch);
  }

  /**
   * Creates a new SpanBatchSender with the given supplier of HttpPoster impl and a BaseConfig
   * instance, with all configuration NOT in BaseConfig being default.
   *
   * @param httpPosterCreator A supplier that returns an HttpPoster for this SpanBatchSender to use.
   * @param baseConfig basic configuration for the sender
   * @return a shiny new SpanBatchSender instance
   */
  public static SpanBatchSender create(
      Supplier<HttpPoster> httpPosterCreator, BaseConfig baseConfig) {
    return SpanBatchSender.create(
        SpanBatchSenderFactory.fromHttpImplementation(httpPosterCreator)
            .configureWith(baseConfig)
            .build());
  }

  /**
   * Build the final {@link SpanBatchSender}.
   *
   * @param configuration new relict rest api ingest configurations
   * @return the fully configured SpanBatchSender object
   */
  public static SpanBatchSender create(SenderConfiguration configuration) {
    Utils.verifyNonNull(configuration.getApiKey(), "API key cannot be null");
    Utils.verifyNonNull(configuration.getHttpPoster(), "an HttpPoster implementation is required.");

    URL url = configuration.getEndpointUrl();

    SpanBatchMarshaller marshaller =
        new SpanBatchMarshaller(
            new SpanJsonCommonBlockWriter(new AttributesJson()),
            new SpanJsonTelemetryBlockWriter(new AttributesJson()));
    BatchDataSender sender =
        new BatchDataSender(
            configuration.getHttpPoster(),
            configuration.getApiKey(),
            url,
            configuration.isAuditLoggingEnabled(),
            configuration.getSecondaryUserAgent());

    return new SpanBatchSender(marshaller, sender);
  }

  public static SenderConfiguration.SenderConfigurationBuilder configurationBuilder() {
    return SenderConfiguration.builder(DEFAULT_URL, SPANS_PATH);
  }
}
