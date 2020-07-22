/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.telemetry.events;

import com.newrelic.telemetry.BaseConfig;
import com.newrelic.telemetry.EventBatchSenderFactory;
import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.SenderConfiguration;
import com.newrelic.telemetry.SenderConfiguration.SenderConfigurationBuilder;
import com.newrelic.telemetry.TelemetryBatch;
import com.newrelic.telemetry.events.json.EventBatchMarshaller;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.transport.BatchDataSender;
import com.newrelic.telemetry.util.Utils;
import java.net.URL;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBatchSender {
  private static final String EVENTS_PATH = "/v1/accounts/events";
  private static final String DEFAULT_URL = "https://trace-api.newrelic.com/";

  private static final Logger logger = LoggerFactory.getLogger(EventBatchSender.class);

  private final EventBatchMarshaller marshaller;
  private final BatchDataSender sender;

  EventBatchSender(EventBatchMarshaller marshaller, BatchDataSender sender) {
    this.marshaller = marshaller;
    this.sender = sender;
  }

  /**
   * Send a batch of events to New Relic.
   *
   * @param batch The batch to send. This batch will be drained of accumulated events as a part of
   *     this process.
   * @return The response from the ingest API.
   * @throws ResponseException In cases where the batch is unable to be successfully sent, one of
   *     the subclasses of {@link ResponseException} will be thrown. See the documentation on that
   *     hierarchy for details on the recommended ways to respond to those exceptions.
   */
  public Response sendBatch(EventBatch batch) throws ResponseException {
    if (batch == null || batch.size() == 0) {
      logger.debug("Skipped sending of an empty event batch.");
      return new Response(202, "Ignored", "Empty batch");
    }
    logger.debug(
        "Sending an event batch (number of events: {}) to the New Relic event ingest endpoint)",
        batch.size());
    String json = marshaller.toJson(batch);

    return sender.send(json);
  }

  @SuppressWarnings("unchecked")
  Response splitBatchAndSend(EventBatch batch, BlockingDeque<TelemetryBatch> queue) {
    logger.info(
        "Tried to send a too-large event batch (number of events: {}) to the New Relic event ingest endpoint. Splitting)",
        batch.size());
    List<TelemetryBatch<Event>> twoBatches = batch.split();
    Response response = null;

    // Preserve in-order processing
    queue.addFirst(twoBatches.get(1));
    queue.addFirst(twoBatches.get(0));
    while (!queue.isEmpty()) {
      final EventBatch eb = (EventBatch) queue.pollFirst();
      final String json = marshaller.toJson(eb);

      try {
        response = sender.send(json);
      } catch (RetryWithSplitException splitx) {
        response = splitBatchAndSend(eb, queue);
      } catch (ResponseException rsx) {
        // We have to log and swallow this exception as there may be other split batches
        // might succeed at sending
        logger.info(
            "Failed to send a split event batch to the New Relic event ingest endpoint. Exception: {}",
            rsx);
      }
    }
    if (response == null) {
      response =
          new Response(
              200, "OK", "Large payload was split - check log in case of dropped sub-batch");
    }

    return response;
  }

  /**
   * Creates a new EventBatchSender with the given supplier of HttpPoster impl and a BaseConfig
   * instance, with all configuration NOT in BaseConfig being default.
   *
   * @param httpPosterCreator A supplier that returns an HttpPoster for this EventBatchSender to
   *     use.
   * @param baseConfig basic configuration for the sender
   * @return a shiny new EventBatchSender instance
   */
  public static EventBatchSender create(
      Supplier<HttpPoster> httpPosterCreator, BaseConfig baseConfig) {
    return EventBatchSender.create(
        EventBatchSenderFactory.fromHttpImplementation(httpPosterCreator)
            .configureWith(baseConfig)
            .build());
  }

  public static EventBatchSender create(SenderConfiguration configuration) {
    Utils.verifyNonNull(configuration.getApiKey(), "API key cannot be null");
    Utils.verifyNonNull(configuration.getHttpPoster(), "an HttpPoster implementation is required.");

    URL url = configuration.getEndpointUrl();

    EventBatchMarshaller marshaller = new EventBatchMarshaller();

    BatchDataSender sender =
        new BatchDataSender(
            configuration.getHttpPoster(),
            configuration.getApiKey(),
            url,
            configuration.isAuditLoggingEnabled(),
            configuration.getSecondaryUserAgent());

    return new EventBatchSender(marshaller, sender);
  }

  public static SenderConfigurationBuilder configurationBuilder() {
    return SenderConfiguration.builder(DEFAULT_URL, EVENTS_PATH);
  }
}
