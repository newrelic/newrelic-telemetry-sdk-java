/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import com.newrelic.telemetry.exceptions.DiscardBatchException;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.exceptions.RetryWithBackoffException;
import com.newrelic.telemetry.exceptions.RetryWithRequestedWaitException;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.http.HttpResponse;
import com.newrelic.telemetry.util.Utils;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
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

/** Manages the sending of {@link MetricBatch} instances to the New Relic Metrics API. */
public class MetricBatchSender {
  private static final Logger logger = LoggerFactory.getLogger(MetricBatchSender.class);

  private static final String metricsPath = "/metric/v1";
  private static final String MEDIA_TYPE = "application/json; charset=utf-8";

  private final MetricBatchJsonGenerator metricJsonGenerator;
  private final HttpPoster client;

  private final URL metricsUrl;
  private final String apiKey;
  private final boolean auditLoggingEnabled;

  private static final String USER_AGENT_VALUE;

  static {
    Package thisPackage = MetricBatchSender.class.getPackage();
    String implementationVersion =
        Optional.ofNullable(thisPackage.getImplementationVersion()).orElse("Unknown Version");
    USER_AGENT_VALUE = "NewRelic-Java-TelemetrySDK/" + implementationVersion;
  }

  private MetricBatchSender(Builder builder, HttpPoster httpPoster) {
    metricJsonGenerator =
        new MetricBatchJsonGenerator(builder.jsonGenerator, builder.attributesJson);
    apiKey = builder.apiKey;
    metricsUrl = builder.metricsUrl;
    client = httpPoster;
    auditLoggingEnabled = builder.auditLoggingEnabled;
  }

  /**
   * Create a new MetricBatchSender with the New Relic API key and the default values for the ingest
   *
   * <p>endpoint and call timeout.
   *
   * @param apiKey Your New Relic Insights Insert API key
   * @param attributeJson
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  public static Builder builder(
      String apiKey,
      HttpPoster httpPoster,
      MetricJsonGenerator jsonGenerator,
      AttributesJson attributeJson) {
    return new Builder(apiKey, httpPoster, jsonGenerator, attributeJson);
  }

  public static class Builder {
    // Required parameters
    private final String apiKey;
    private final MetricJsonGenerator jsonGenerator;
    private final AttributesJson attributesJson;
    private HttpPoster httpPoster;

    private URL metricsUrl;
    private boolean auditLoggingEnabled = false;

    /**
     * Create a new MetricBatchSender with the New Relic API key and the default values for the
     * ingest endpoint and call timeout.
     *
     * @param apiKey Your New Relic Insights Insert API key
     * @see <a
     *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
     *     Relic API Keys</a>
     */
    public Builder(
        String apiKey,
        HttpPoster httpPoster,
        MetricJsonGenerator jsonGenerator,
        AttributesJson attributesJson) {
      Utils.verifyNonNull(apiKey, "API key cannot be null");
      Utils.verifyNonNull(httpPoster, "an HttpPoster implementation is required.");
      Utils.verifyNonNull(jsonGenerator, "an MetricJsonGenerator implementation is required.");
      this.httpPoster = httpPoster;
      this.apiKey = apiKey;
      this.jsonGenerator = jsonGenerator;
      this.attributesJson = attributesJson;

      try {
        metricsUrl = constructMetricsUrlWithHost(URI.create("https://metric-api.newrelic.com/"));
      } catch (MalformedURLException e) {
        throw new UncheckedIOException("Bad hardcoded URL", e);
      }
    }

    /**
     * Set a URI to override the default ingest endpoint.
     *
     * @param uriOverride The scheme, host, and port that should be used for the Metrics API
     *     endpoint. The path component of this parameter is unused.
     * @return the Builder
     * @throws MalformedURLException This is thrown when the provided URI is malformed.
     */
    public Builder uriOverride(URI uriOverride) throws MalformedURLException {
      this.metricsUrl = constructMetricsUrlWithHost(uriOverride);
      return this;
    }

    /**
     * Turns on audit logging. Payloads sent will be logged at the DEBUG level. Please note that if
     * your payloads contain sensitive information, that information will be logged wherever your
     * logs are configured.
     */
    public Builder enableAuditLogging() {
      this.auditLoggingEnabled = true;
      return this;
    }

    /**
     * Build the final {@link MetricBatchSender}.
     *
     * @return the fully configured MetricBatchSender object
     */
    public MetricBatchSender build() {
      Utils.verifyNonNull(metricsUrl, "You must specify a base URL for the New Relic metric API.");
      return new MetricBatchSender(this, httpPoster);
    }
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
      logger.debug("Tried to send a null or empty metric batch");
      return new Response(202, "Ignored", "Empty batch");
    }
    logger.debug(
        "Sending a metric batch (number of metrics: {}) to the New Relic metric ingest API (endpoint: {})",
        batch.size(),
        metricsUrl);
    byte[] payload;
    try {
      if (auditLoggingEnabled) {
        logger.debug(metricJsonGenerator.generateJson(batch));
      }
      payload = generateCompressedPayload(batch);
    } catch (IOException e) {
      logger.error("Failed to serialize the metric batch for sending to the ingest API", e);
      throw new DiscardBatchException();
    }

    Map<String, String> headers = new HashMap<>();
    headers.put("Api-Key", apiKey);
    headers.put("Content-Encoding", "gzip");
    headers.put("User-Agent", USER_AGENT_VALUE);
    try {
      HttpResponse response = client.post(metricsUrl, headers, payload, MEDIA_TYPE);
      String responseBody = response.getBody();
      logger.debug(
          "Response from New Relic metric ingest API: code: {}, body: {}",
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
              "Response from New Relic metric ingest API: code: {}, body: {}",
              response.getCode(),
              responseBody);
          throw new DiscardBatchException();
        case 413:
          logger.warn(
              "Response from New Relic metric ingest API: code: {}, body: {}",
              response.getCode(),
              responseBody);
          throw new RetryWithSplitException();
        case 429:
          return handle429(response, responseBody);
        default:
          logger.error(
              "Response from New Relic metric ingest API: code: {}, body: {}",
              response.getCode(),
              responseBody);
          throw new RetryWithBackoffException();
      }
    } catch (IOException e) {
      logger.error("IOException while trying to send metric data to New Relic", e);
      throw new RetryWithBackoffException();
    }
  }

  private Response handle429(HttpResponse response, String responseBody)
      throws RetryWithBackoffException, RetryWithRequestedWaitException {
    Map<String, List<String>> responseHeaders = response.getHeaders();

    Optional<List<String>> retryAfter = findHeader(responseHeaders, "retry-after");
    int retryAfterSeconds = getRetryAfterValue(response, responseBody, retryAfter);
    logger.warn(
        "Response from New Relic metric ingest API: code: {}, body: {}, retry-after: {}",
        response.getCode(),
        responseBody,
        retryAfterSeconds);
    throw new RetryWithRequestedWaitException(retryAfterSeconds, TimeUnit.SECONDS);
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
          "Unparseable retry-after header from New Relic metric ingest API: code: {}, body: {}, retry-after: {}",
          response.getCode(),
          responseBody,
          response.getHeaders().get("retry-after"));
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

  private byte[] generateCompressedPayload(MetricBatch batch) throws IOException {
    String result = metricJsonGenerator.generateJson(batch);
    ByteArrayOutputStream compressedOutput = new ByteArrayOutputStream();
    GZIPOutputStream gzipOutputStream = new GZIPOutputStream(compressedOutput);
    gzipOutputStream.write(result.getBytes(StandardCharsets.UTF_8));
    gzipOutputStream.close();

    return compressedOutput.toByteArray();
  }

  private static URL constructMetricsUrlWithHost(URI hostUri) throws MalformedURLException {
    return hostUri.resolve(metricsPath).toURL();
  }
}
