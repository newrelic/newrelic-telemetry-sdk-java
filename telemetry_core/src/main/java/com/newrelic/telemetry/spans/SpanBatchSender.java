/*
 * --------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.json.TelemetryBatchJson;
import com.newrelic.telemetry.json.TypeDispatchingJsonCommonBlockWriter;
import com.newrelic.telemetry.json.TypeDispatchingJsonTelemetryBlockWriter;
import com.newrelic.telemetry.spans.json.SpanJsonCommonBlockWriter;
import com.newrelic.telemetry.spans.json.SpanJsonTelemetryBlockWriter;
import com.newrelic.telemetry.transport.BatchDataSender;
import com.newrelic.telemetry.util.Utils;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Manages the sending of {@link SpanBatch} instances to the New Relic Spans API. */
public class SpanBatchSender {

  private static final Logger logger = LoggerFactory.getLogger(SpanBatchSender.class);

  private static final String spansPath = "/trace/v1";

  private final TelemetryBatchJson telemetryBatchJson;

  private final boolean auditLoggingEnabled;

  private final BatchDataSender batchDataSender;

  private SpanBatchSender(Builder builder, HttpPoster httpPoster) {
    telemetryBatchJson =
        new TelemetryBatchJson(
            new TypeDispatchingJsonCommonBlockWriter(
                null, new SpanJsonCommonBlockWriter(builder.attributesJson)),
            new TypeDispatchingJsonTelemetryBlockWriter(
                null, new SpanJsonTelemetryBlockWriter(builder.attributesJson)));

    auditLoggingEnabled = builder.auditLoggingEnabled;
    batchDataSender = new BatchDataSender(httpPoster, builder.apiKey, builder.spansUrl);
  }

  /**
   * Create a new SpanBatchSender with the New Relic API key and the default values for the ingest
   * endpoint and call timeout.
   *
   * @param apiKey Your New Relic Insights Insert API key
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  public static Builder builder(
      String apiKey, HttpPoster httpPoster, AttributesJson attributeJson) {
    return new Builder(apiKey, httpPoster, attributeJson);
  }

  public static class Builder {

    // Required parameters
    private final String apiKey;
    private final AttributesJson attributesJson;
    private HttpPoster httpPoster;

    private URL spansUrl;
    private boolean auditLoggingEnabled = false;

    /**
     * Create a new SpanBatchSender with the New Relic API key and the default values for the ingest
     * endpoint and call timeout.
     *
     * @param apiKey Your New Relic Insights Insert API key
     * @see <a
     *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
     *     Relic API Keys</a>
     */
    public Builder(String apiKey, HttpPoster httpPoster, AttributesJson attributesJson) {
      this.httpPoster = httpPoster;
      this.apiKey = apiKey;
      this.attributesJson = attributesJson;

      try {
        spansUrl = constructSpansUrlWithHost(URI.create("https://trace-api.newrelic.com/"));
      } catch (MalformedURLException e) {
        throw new UncheckedIOException("Bad hardcoded URL", e);
      }
    }

    /**
     * Set a URI to override the default ingest endpoint.
     *
     * @param uriOverride The scheme, host, and port that should be used for the Spans API endpoint.
     *     The path component of this parameter is unused.
     * @return the Builder
     * @throws MalformedURLException This is thrown when the provided URI is malformed.
     */
    public Builder uriOverride(URI uriOverride) throws MalformedURLException {
      this.spansUrl = constructSpansUrlWithHost(uriOverride);
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
     * Build the final {@link SpanBatchSender}.
     *
     * @return the fully configured SpanBatchSender object
     */
    public SpanBatchSender build() {
      Utils.verifyNonNull(spansUrl, "You must specify a base URL for the New Relic span API.");
      Utils.verifyNonNull(apiKey, "API key cannot be null");
      Utils.verifyNonNull(httpPoster, "an HttpPoster implementation is required.");

      return new SpanBatchSender(this, httpPoster);
    }
  }

  /**
   * Send a batch of spans to New Relic.
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
      logger.debug("Tried to send a null or empty span batch");
      return new Response(202, "Ignored", "Empty batch");
    }
    logger.debug(
        "Sending a span batch (number of spans: {}) to the New Relic span ingest endpoint)",
        batch.size());
    String json = generateJsonPayload(batch);
    return batchDataSender.send(json);
  }

  private String generateJsonPayload(SpanBatch batch) {
    String json = telemetryBatchJson.toJson(batch);
    if (auditLoggingEnabled) {
      logger.debug(json);
    }
    return json;
  }

  private static URL constructSpansUrlWithHost(URI hostUri) throws MalformedURLException {
    return hostUri.resolve(spansPath).toURL();
  }
}
