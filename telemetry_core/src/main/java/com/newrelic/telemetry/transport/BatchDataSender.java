/*
 * --------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.transport;

import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.exceptions.DiscardBatchException;
import com.newrelic.telemetry.exceptions.RetryWithBackoffException;
import com.newrelic.telemetry.exceptions.RetryWithRequestedWaitException;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.http.HttpResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchDataSender {
  private static final Logger logger = LoggerFactory.getLogger(BatchDataSender.class);
  private static final String MEDIA_TYPE = "application/json; charset=utf-8";

  private static final String USER_AGENT_VALUE;

  private final HttpPoster client;
  private final String apiKey;
  private final URL endpointURl;
  private final boolean auditLoggingEnabled;

  static {
    Package thisPackage = BatchDataSender.class.getPackage();
    String implementationVersion =
        Optional.ofNullable(thisPackage.getImplementationVersion()).orElse("Unknown Version");
    USER_AGENT_VALUE = "NewRelic-Java-TelemetrySDK/" + implementationVersion;
  }

  public BatchDataSender(HttpPoster client, String apiKey, URL endpointURl,
      boolean auditLoggingEnabled) {
    this.client = client;
    this.apiKey = apiKey;
    this.endpointURl = endpointURl;
    this.auditLoggingEnabled = auditLoggingEnabled;
  }

  public Response send(String json)
      throws DiscardBatchException, RetryWithSplitException, RetryWithBackoffException,
      RetryWithRequestedWaitException {
    if(auditLoggingEnabled){
      logger.debug("Sending json: " + json);
    }
    byte[] payload = generatePayload(json);

    return sendPayload(payload);
  }

  private byte[] generatePayload(String json) throws DiscardBatchException {
    byte[] payload;
    try {
      payload = compressJson(json);
    } catch (IOException e) {
      logger.error("Failed to serialize the batch for sending to the ingest API", e);
      throw new DiscardBatchException();
    }
    return payload;
  }

  private byte[] compressJson(String result) throws IOException {
    ByteArrayOutputStream compressedOutput = new ByteArrayOutputStream();
    GZIPOutputStream gzipOutputStream = new GZIPOutputStream(compressedOutput);
    gzipOutputStream.write(result.getBytes(StandardCharsets.UTF_8));
    gzipOutputStream.close();

    return compressedOutput.toByteArray();
  }

  private Response sendPayload(byte[] payload)
      throws DiscardBatchException, RetryWithSplitException, RetryWithBackoffException,
      RetryWithRequestedWaitException {
    Map<String, String> headers = new HashMap<>();
    headers.put("Api-Key", apiKey);
    headers.put("Content-Encoding", "gzip");
    headers.put("User-Agent", USER_AGENT_VALUE);
    try {
      HttpResponse response = client.post(endpointURl, headers, payload, MEDIA_TYPE);
      String responseBody = response.getBody();
      logger.debug(
          "Response from New Relic ingest API: code: {}, body: {}",
          response.getCode(),
          response.getBody());
      if (response.getCode() == 202) {
        return new Response(response.getCode(), response.getMessage(), responseBody);
      }
      switch (response.getCode()) {
        case 400:
        case 403:
        case 404:
        case 405:
        case 411:
          logger.warn(
              "Response from New Relic ingest API: code: {}, body: {}",
              response.getCode(),
              responseBody);
          throw new DiscardBatchException();
        case 413:
          logger.warn(
              "Response from New Relic ingest API: code: {}, body: {}",
              response.getCode(),
              responseBody);
          throw new RetryWithSplitException();
        case 429:
          return handle429(response, responseBody);
        default:
          logger.error(
              "Response from New Relic ingest API: code: {}, body: {}",
              response.getCode(),
              responseBody);
          throw new RetryWithBackoffException();
      }
    } catch (IOException e) {
      logger.error("IOException while trying to send data to New Relic", e);
      throw new RetryWithBackoffException();
    }
  }

  private Optional<List<String>> findHeader(
      Map<String, List<String>> responseHeaders, String headerName) {
    return responseHeaders
        .keySet()
        .stream()
        .filter(headerName::equalsIgnoreCase)
        .findAny()
        .map(responseHeaders::get)
        .filter(values -> !values.isEmpty());
  }

  private int getRetryAfterValue(
      HttpResponse response, String responseBody, Optional<List<String>> retryAfter)
      throws RetryWithBackoffException {
    if (!retryAfter.isPresent()) {
      logger.warn("429 received from the backend with no retry-after header. Using 10s");
      return 10;
    }

    try {
      return Integer.parseInt(retryAfter.get().get(0));
    } catch (NumberFormatException e) {
      logger.warn(
          "Unparseable retry-after header from New Relic ingest API: code: {}, body: {}, retry-after: {}",
          response.getCode(),
          responseBody,
          response.getHeaders().get("retry-after"));
      throw new RetryWithBackoffException();
    }
  }

  private Response handle429(HttpResponse response, String responseBody)
      throws RetryWithBackoffException, RetryWithRequestedWaitException {
    Map<String, List<String>> responseHeaders = response.getHeaders();

    Optional<List<String>> retryAfter = findHeader(responseHeaders, "retry-after");
    int retryAfterSeconds = getRetryAfterValue(response, responseBody, retryAfter);
    logger.warn(
        "Response from New Relic ingest API: code: {}, body: {}, retry-after: {}",
        response.getCode(),
        responseBody,
        retryAfterSeconds);
    throw new RetryWithRequestedWaitException(retryAfterSeconds, TimeUnit.SECONDS);
  }
}
