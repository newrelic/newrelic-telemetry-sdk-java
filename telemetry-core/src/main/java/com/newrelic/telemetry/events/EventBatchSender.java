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
import com.newrelic.telemetry.events.json.EventBatchMarshaller;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.transport.BatchDataSender;
import com.newrelic.telemetry.util.Utils;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBatchSender {
  private static final String EVENTS_PATH = "/v1/accounts/events";
  private static final String DEFAULT_URL = "https://insights-collector.newrelic.com";
  private static final String EUROPEAN_URL = "https://insights-collector.eu01.nr-data.net";
  private static final Response EMPTY_BATCH_RESPONSE = new Response(202, "Ignored", "Empty batch");

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
      return EMPTY_BATCH_RESPONSE;
    }
    logger.debug(
        "Sending an event batch (number of events: {}) to the New Relic event ingest endpoint)",
        batch.size());
    String json = marshaller.toJson(batch);
    return sender.send(json, batch);
  }

  /**
   * Send batches of events to New Relic.
   *
   * @param eventBatches An ArrayList where each element is an EventBatch. Each EventBatch will be
   *     sent to New Relic one by one. Each batch will be drained of accumulated events as a part of
   *     this process.
   * @return The response from the ingest API.
   * @throws ResponseException In cases where the batch is unable to be successfully sent, one of
   *     the subclasses of {@link ResponseException} will be thrown. See the documentation on that
   *     hierarchy for details on the recommended ways to respond to those exceptions.
   */
  public Response sendBatch(ArrayList<EventBatch> eventBatches) throws ResponseException {
    Response response = new Response(200, "success", "sent data");
    for (EventBatch batch : eventBatches) {
      response = sendBatch(batch);

      // If there is an issue with a batch, stop sending batches
      if ((response.getStatusCode() != 200)) {
        return response;
      }
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

    String userRegion = configuration.getRegion();

    String defaultUrl = DEFAULT_URL + EVENTS_PATH;
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

    EventBatchMarshaller marshaller = new EventBatchMarshaller();

    BatchDataSender sender =
        new BatchDataSender(
            configuration.getHttpPoster(),
            configuration.getApiKey(),
            url,
            configuration.isAuditLoggingEnabled(),
            configuration.getSecondaryUserAgent(),
            configuration.useLicenseKey());

    return new EventBatchSender(marshaller, sender);
  }

  public static URL returnEndpoint(String userRegion) throws MalformedURLException {
    URL url = null;

    if (userRegion.equals("US")) {
      try {
        url = new URL(DEFAULT_URL + EVENTS_PATH);
        return url;
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    } else if (userRegion.equals("EU")) {
      try {
        url = new URL(EUROPEAN_URL + EVENTS_PATH);
        return url;
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }
    throw new MalformedURLException(
        "A valid region (EU or US) needs to be added to generate the right endpoint");
  }

  public static SenderConfigurationBuilder configurationBuilder() {
    return SenderConfiguration.builder(DEFAULT_URL, EVENTS_PATH);
  }
}
