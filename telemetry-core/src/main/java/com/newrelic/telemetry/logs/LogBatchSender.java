/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.logs;

import com.newrelic.telemetry.BaseConfig;
import com.newrelic.telemetry.LogBatchSenderFactory;
import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.SenderConfiguration;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.logs.json.LogBatchMarshaller;
import com.newrelic.telemetry.logs.json.LogJsonCommonBlockWriter;
import com.newrelic.telemetry.logs.json.LogJsonTelemetryBlockWriter;
import com.newrelic.telemetry.transport.BatchDataSender;
import com.newrelic.telemetry.util.Utils;
import java.net.URL;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Manages the sending of {@link LogBatch} instances to the New Relic Logs API. */
public class LogBatchSender {

  private static final String LOGS_PATH = "/log/v1";
  private static final String DEFAULT_URL = "https://log-api.newrelic.com/";

  private static final Logger logger = LoggerFactory.getLogger(LogBatchSender.class);

  private final LogBatchMarshaller marshaller;
  private final BatchDataSender sender;

  /**
   * Creates a log batch sender that knows how to marshall log batches and send them
   *
   * @param marshaller Defines how to marshall log batches
   * @param sender Sends log batches
   */
  LogBatchSender(LogBatchMarshaller marshaller, BatchDataSender sender) {
    this.marshaller = marshaller;
    this.sender = sender;
  }

  /**
   * Send a batch of logs to New Relic.
   *
   * @param batch The batch to send. This batch will be drained of accumulated logs as a part of
   *     this process.
   * @return The response from the ingest API.
   * @throws ResponseException In cases where the batch is unable to be successfully sent, one of
   *     the subclasses of {@link ResponseException} will be thrown. See the documentation on that
   *     hierarchy for details on the recommended ways to respond to those exceptions.
   */
  public Response sendBatch(LogBatch batch) throws ResponseException {
    if (batch == null || batch.size() == 0) {
      logger.debug("Skipped sending a null or empty log batch");
      return new Response(202, "Ignored", "Empty batch");
    }
    logger.debug(
        "Sending a log batch (number of logs: {}) to the New Relic log ingest endpoint)",
        batch.size());
    String json = marshaller.toJson(batch);
    return sender.send(json, batch.getUuid());
  }

  /**
   * Creates a new LogBatchSender with the given supplier of HttpPoster impl and a BaseConfig
   * instance, with all configuration NOT in BaseConfig being default.
   *
   * @param httpPosterCreator A supplier that returns an HttpPoster for this LogBatchSender to use.
   * @param baseConfig basic configuration for the sender
   * @return a shiny new LogBatchSender instance
   */
  public static LogBatchSender create(
      Supplier<HttpPoster> httpPosterCreator, BaseConfig baseConfig) {
    return create(
        LogBatchSenderFactory.fromHttpImplementation(httpPosterCreator)
            .configureWith(baseConfig)
            .build());
  }

  /**
   * Build the final {@link LogBatchSender}.
   *
   * @return the fully configured LogBatchSender object
   */
  public static LogBatchSender create(SenderConfiguration configuration) {
    Utils.verifyNonNull(configuration.getApiKey(), "API key cannot be null");
    Utils.verifyNonNull(configuration.getHttpPoster(), "an HttpPoster implementation is required.");

    URL url = configuration.getEndpointUrl();

    LogBatchMarshaller marshaller =
        new LogBatchMarshaller(
            new LogJsonCommonBlockWriter(new AttributesJson()),
            new LogJsonTelemetryBlockWriter(new AttributesJson()));
    BatchDataSender sender =
        new BatchDataSender(
            configuration.getHttpPoster(),
            configuration.getApiKey(),
            url,
            configuration.isAuditLoggingEnabled(),
            configuration.getSecondaryUserAgent());

    return new LogBatchSender(marshaller, sender);
  }

  public static SenderConfiguration.SenderConfigurationBuilder configurationBuilder() {
    return SenderConfiguration.builder(DEFAULT_URL, LOGS_PATH);
  }
}
