/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.exceptions.DiscardBatchException;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.json.TelemetryBatchJson;
import com.newrelic.telemetry.json.TypeDispatchingJsonCommonBlockWriter;
import com.newrelic.telemetry.json.TypeDispatchingJsonTelemetryBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricBatchJsonCommonBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricBatchJsonTelemetryBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricToJson;
import com.newrelic.telemetry.transport.BatchDataSender;
import com.newrelic.telemetry.util.Utils;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Manages the sending of {@link MetricBatch} instances to the New Relic Metrics API. */
public class MetricBatchSender {

  private static final Logger logger = LoggerFactory.getLogger(MetricBatchSender.class);

  private static final String metricsPath = "/metric/v1";
  private static final String MEDIA_TYPE = "application/json; charset=utf-8";

  private final TelemetryBatchJson telemetryBatchJson;

  private final boolean auditLoggingEnabled;

  private final BatchDataSender batchDataSender;

  private MetricBatchSender(Builder builder, HttpPoster httpPoster) {
    telemetryBatchJson =
        new TelemetryBatchJson(
            new TypeDispatchingJsonCommonBlockWriter<>(
                new MetricBatchJsonCommonBlockWriter(builder.attributesJson), null),
            new TypeDispatchingJsonTelemetryBlockWriter<>(
                new MetricBatchJsonTelemetryBlockWriter(builder.metricToJson), null));

    auditLoggingEnabled = builder.auditLoggingEnabled;
    batchDataSender = new BatchDataSender(httpPoster, builder.apiKey, builder.metricsUrl);
  }

  /**
   * Create a new MetricBatchSender with the New Relic API key and the default values for the ingest
   * endpoint and call timeout.
   *
   * @param apiKey Your New Relic Insights Insert API key
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  public static Builder builder(
      String apiKey,
      HttpPoster httpPoster,
      MetricToJson metricToJson,
      AttributesJson attributeJson) {
    return new Builder(apiKey, httpPoster, metricToJson, attributeJson);
  }

  public static class Builder {

    // Required parameters
    private final String apiKey;
    private final MetricToJson metricToJson;
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
        MetricToJson metricToJson,
        AttributesJson attributesJson) {
      this.httpPoster = httpPoster;
      this.apiKey = apiKey;
      this.metricToJson = metricToJson;
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
      Utils.verifyNonNull(apiKey, "API key cannot be null");
      Utils.verifyNonNull(httpPoster, "an HttpPoster implementation is required.");
      Utils.verifyNonNull(metricToJson, "an MetricToJson implementation is required.");

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
        "Sending a metric batch (number of metrics: {}) to the New Relic metric ingest endpoint)",
        batch.size());
    String json = generateJsonPayload(batch);
    return batchDataSender.send(json);
  }

  private String generateJsonPayload(MetricBatch batch) throws DiscardBatchException {
    String json = telemetryBatchJson.toJson(batch);
    if (auditLoggingEnabled) {
      logger.debug(json);
    }
    return json;
  }

  private static URL constructMetricsUrlWithHost(URI hostUri) throws MalformedURLException {
    return hostUri.resolve(metricsPath).toURL();
  }
}
