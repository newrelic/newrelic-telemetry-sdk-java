package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.metrics.json.MetricBatchJsonCommonBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricBatchJsonTelemetryBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricBatchMarshaller;
import com.newrelic.telemetry.metrics.json.MetricToJson;
import com.newrelic.telemetry.transport.BatchDataSender;
import com.newrelic.telemetry.util.Utils;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class MetricBatchSenderBuilder {

  private static final String metricsPath = "/metric/v1";

  private String apiKey;
  private MetricToJson metricToJson;
  private AttributesJson attributesJson;
  private HttpPoster httpPoster;
  private URL metricsUrl;
  private boolean auditLoggingEnabled = false;

  /**
   * Build the final {@link MetricBatchSender}.
   *
   * @return the fully configured MetricBatchSender object
   */
  public MetricBatchSender build() {
    Utils.verifyNonNull(apiKey, "API key cannot be null");
    Utils.verifyNonNull(httpPoster, "an HttpPoster implementation is required.");
    Utils.verifyNonNull(metricToJson, "an MetricToJson implementation is required.");
    Utils.verifyNonNull(attributesJson, "an AttributesJson implementation is required.");

    URL url = getOrDefaultMetricsUrl();

    MetricBatchMarshaller marshaller =
        new MetricBatchMarshaller(
            new MetricBatchJsonCommonBlockWriter(attributesJson),
            new MetricBatchJsonTelemetryBlockWriter(metricToJson));
    BatchDataSender sender = new BatchDataSender(httpPoster, apiKey, url, auditLoggingEnabled);

    return new MetricBatchSender(marshaller, sender);
  }

  private URL getOrDefaultMetricsUrl() {
    if (metricsUrl == null) {
      try {
        return constructMetricsUrlWithHost(URI.create("https://metric-api.newrelic.com/"));
      } catch (MalformedURLException e) {
        throw new UncheckedIOException("Bad hardcoded URL", e);
      }
    }
    return metricsUrl;
  }

  /**
   * Set a URI to override the default ingest endpoint.
   *
   * @param uriOverride The scheme, host, and port that should be used for the Metrics API endpoint.
   *     The path component of this parameter is unused.
   * @return the Builder
   * @throws MalformedURLException This is thrown when the provided URI is malformed.
   */
  public MetricBatchSenderBuilder uriOverride(URI uriOverride) throws MalformedURLException {
    this.metricsUrl = constructMetricsUrlWithHost(uriOverride);
    return this;
  }

  /**
   * Turns on audit logging. Payloads sent will be logged at the DEBUG level. Please note that if
   * your payloads contain sensitive information, that information will be logged wherever your logs
   * are configured.
   */
  public MetricBatchSenderBuilder enableAuditLogging() {
    this.auditLoggingEnabled = true;
    return this;
  }

  public MetricBatchSenderBuilder apiKey(String apiKey) {
    this.apiKey = apiKey;
    return this;
  }

  public MetricBatchSenderBuilder metricToJson(MetricToJson metricToJson) {
    this.metricToJson = metricToJson;
    return this;
  }

  public MetricBatchSenderBuilder attributesJson(AttributesJson attributesJson) {
    this.attributesJson = attributesJson;
    return this;
  }

  public MetricBatchSenderBuilder httpPoster(HttpPoster httpPoster) {
    this.httpPoster = httpPoster;
    return this;
  }

  private static URL constructMetricsUrlWithHost(URI hostUri) throws MalformedURLException {
    return hostUri.resolve(metricsPath).toURL();
  }
}
