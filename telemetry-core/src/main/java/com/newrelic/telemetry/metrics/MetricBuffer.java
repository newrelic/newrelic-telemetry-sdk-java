/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.metrics.json.MetricToJsonStatic;
import com.newrelic.telemetry.util.IngestWarnings;
import com.newrelic.telemetry.util.Utils;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.LoggerFactory;

/**
 * A buffer for collecting {@link Metric Metrics}.
 *
 * <p>One instance of this class can collect many {@link Metric Metrics}. To send them to the
 * Metrics API, call {@link #createBatch()} and then {@link
 * MetricBatchSender#sendBatch(MetricBatch)}.
 *
 * <p>This class is thread-safe.
 */
public final class MetricBuffer {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MetricBuffer.class);
  private final Queue<Metric> metrics = new ConcurrentLinkedQueue<>();
  private final IngestWarnings ingestWarnings = new IngestWarnings();
  private final Attributes commonAttributes;
  private final boolean splitBatch;

  /**
   * Create a new buffer with the provided common set of attributes.
   *
   * @param commonAttributes These attributes will be appended (by the New Relic backend) to every
   *     {@link Metric} in this buffer.
   */
  public MetricBuffer(Attributes commonAttributes) {
    this(commonAttributes, false);
  }

  /**
   * Create a new buffer with the provided common set of attributes.
   *
   * @param commonAttributes These attributes will be appended (by the New Relic backend) to every
   *     {@link Metric} in this buffer.
   * @param splitOnSizeLimit Flag to indicate whether to split batch when size limit is hit.
   */
  public MetricBuffer(Attributes commonAttributes, boolean splitOnSizeLimit) {
    this.commonAttributes = Utils.verifyNonNull(commonAttributes);
    this.splitBatch = splitOnSizeLimit;
  }

  /**
   * Append a {@link Count} to this buffer, to be sent in the next {@link MetricBatch}.
   *
   * @param countMetric The new {@link Count} instance to be sent.
   */
  public void addMetric(Count countMetric) {
    Map<String, Object> attributes = countMetric.getAttributes();
    ingestWarnings.raiseIngestWarnings(attributes, "Metric");
    metrics.add(countMetric);
  }

  /**
   * Append a {@link Gauge} to this buffer, to be sent in the next {@link MetricBatch}.
   *
   * @param gaugeMetric The new {@link Gauge} instance to be sent.
   */
  public void addMetric(Gauge gaugeMetric) {
    Map<String, Object> attributes = gaugeMetric.getAttributes();
    ingestWarnings.raiseIngestWarnings(attributes, "Metric");
    metrics.add(gaugeMetric);
  }

  /**
   * Append a {@link Summary} to this buffer, to be sent in the next {@link MetricBatch}.
   *
   * @param summaryMetric The new {@link Summary} instance to be sent.
   */
  public void addMetric(Summary summaryMetric) {
    Map<String, Object> attributes = summaryMetric.getAttributes();
    ingestWarnings.raiseIngestWarnings(attributes, "Metric");
    metrics.add(summaryMetric);
  }

  /**
   * Get the size of the {@link Metric Metrics} buffer.
   *
   * @return Size of the {@link Metric Metrics} buffer.
   */
  public int size() {
    return metrics.size();
  }

  /**
   * Creates a new {@link MetricBatch} from the contents of this buffer, then clears the contents of
   * this buffer.
   *
   * <p>{@link Metric Metrics} added to this buffer by other threads during this method call will
   * either be added to the {@link MetricBatch} being created, or will be saved for the next {@link
   * MetricBatch}.
   *
   * @return A new {@link MetricBatch} with an immutable collection of {@link Metric Metrics}.
   */
  public MetricBatch createSingleBatch() {
    logger.debug("Creating metric batch.");
    Collection<Metric> metrics = new ArrayList<>(this.metrics.size());

    // Drain the metric buffer and return the batch
    Metric metric;
    while ((metric = this.metrics.poll()) != null) {
      metrics.add(metric);
    }

    return new MetricBatch(metrics, this.commonAttributes);
  }

  /**
   * Creates an {@link ArrayList<MetricBatch>} from the contents of this buffer, then clears the
   * contents of this buffer.
   *
   * <p>{@link Metric Metrics} are added to a MetricBatch. When each metric is added, the size (in
   * bytes) of the metric is calculated. When the total size of the metrics in the batch exceeds the
   * MAX_UNCOMPRESSED_BATCH_SIZE, the current batch is sent to New Relic, and a new MetricBatch is
   * created. This process repeats until all metrics are removed from the queue.
   *
   * @return An {@link ArrayList<MetricBatch>}. Each {@link MetricBatch} in the ArrayList contains
   *     an immutable collection of {@link Metric Metrics}.
   */
  public ArrayList<MetricBatch> createBatches() {
    logger.debug("Creating metric batch.");
    ArrayList<MetricBatch> metricBatches = new ArrayList<>();
    Collection<Metric> metricsInBatch = new ArrayList<>();

    int currentUncompressedBatchSize = 0;
    int MAX_UNCOMPRESSED_BATCH_SIZE = 180000000;

    // Construct JSON for common attributes and add to uncompressed batch size

    AttributesJson attrsJson = new AttributesJson();
    StringBuilder commonAttributeSb = new StringBuilder();
    commonAttributeSb
        .append("\"common\":")
        .append("{")
        .append("\"attributes\":")
        .append(attrsJson.toJson(commonAttributes.asMap()))
        .append("}");
    String commonJson = commonAttributeSb.toString();

    currentUncompressedBatchSize += commonJson.getBytes(StandardCharsets.UTF_8).length;

    // JSON generation + calculating payload size

    Metric curMetric;
    while ((curMetric = this.metrics.poll()) != null) {

      if (currentUncompressedBatchSize > MAX_UNCOMPRESSED_BATCH_SIZE) {
        MetricBatch m = new MetricBatch(metricsInBatch, this.commonAttributes);
        metricBatches.add(m);
        metricsInBatch = new ArrayList<>();
        currentUncompressedBatchSize = 0;
      }

      if (curMetric instanceof Count) {
        Count curMetricToCount = (Count) curMetric;
        String countJson = MetricToJsonStatic.writeCountJson(curMetricToCount);
        currentUncompressedBatchSize += countJson.getBytes(StandardCharsets.UTF_8).length;
      }

      if (curMetric instanceof Gauge) {
        Gauge curMetricToGauge = (Gauge) curMetric;
        String gaugeJson = MetricToJsonStatic.writeGaugeJson(curMetricToGauge);
        currentUncompressedBatchSize += gaugeJson.getBytes(StandardCharsets.UTF_8).length;
      }

      if (curMetric instanceof Summary) {
        Summary curMetricToSummary = (Summary) curMetric;
        String summaryJson = MetricToJsonStatic.writeSummaryJson(curMetricToSummary);
        currentUncompressedBatchSize += summaryJson.getBytes(StandardCharsets.UTF_8).length;
      }

      metricsInBatch.add(curMetric);
    }
    metricBatches.add(new MetricBatch(metricsInBatch, this.commonAttributes));
    return metricBatches;
  }

  /**
   * Creates an {@link ArrayList<MetricBatch>} by calling {@link #createSingleBatch()} or {@link
   * #createBatches()}. This depends on if the user wants to split batches on size limit or not
   * (splitBatch). If splitBatch = false, {@link #createSingleBatch()} is called. If splitBatch =
   * true, {@link #createBatches()} is called.
   *
   * @return An {@link ArrayList<MetricBatch>}. Each {@link MetricBatch} in the ArrayList contains
   *     an immutable collection of {@link Metric Metrics}.
   */
  public ArrayList<MetricBatch> createBatch() {
    ArrayList<MetricBatch> batches = new ArrayList<MetricBatch>();
    if (splitBatch == false) {
      MetricBatch singleEventBatch = createSingleBatch();
      batches.add(singleEventBatch);
      return batches;
    } else {
      batches = createBatches();
      return batches;
    }
  }

  Queue<Metric> getMetrics() {
    return metrics;
  }

  Attributes getCommonAttributes() {
    return commonAttributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    MetricBuffer that = (MetricBuffer) o;

    if (getMetrics() != null ? !getMetrics().equals(that.getMetrics()) : that.getMetrics() != null)
      return false;
    return getCommonAttributes() != null
        ? getCommonAttributes().equals(that.getCommonAttributes())
        : that.getCommonAttributes() == null;
  }

  @Override
  public int hashCode() {
    int result = getMetrics() != null ? getMetrics().hashCode() : 0;
    result = 31 * result + (getCommonAttributes() != null ? getCommonAttributes().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "MetricBuffer{" + "metrics=" + metrics + ", commonAttributes=" + commonAttributes + '}';
  }

  /**
   * Returns a new Builder instance for help with creating new MetricBatch instances
   *
   * @return a new instance of MetricBatch.Builder
   */
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private final CommonAttributesBuilder commonAttributesBuilder = new CommonAttributesBuilder();

    /**
     * Provides the base collection of common attributes that will be applied to all buffered
     * metrics.
     *
     * @param attributes - common attributes to be applied to all metrics within this buffer
     * @return this builder
     */
    public Builder attributes(Attributes attributes) {
      commonAttributesBuilder.attributes(attributes);
      return this;
    }

    /**
     * Optional. Specify the name of the service that is creating the metrics. The service name will
     * be included in all common attributes as "service.name".
     *
     * @param serviceName - The name of the service
     * @return this builder
     */
    public Builder serviceName(String serviceName) {
      commonAttributesBuilder.serviceName(serviceName);
      return this;
    }

    /**
     * Optional. Specify the name of the instrumentation that provides the metrics, typically a
     * library like "micrometer" or "dropwizard-metrics" or "kamon", for example. This is generally
     * not expected to be called by a user's manual instrumentation code. The instrumentation
     * provider will be included in all common attributes as "instrumentation.provider".
     *
     * @param instrumentationProvider - The name of the instrumentation library
     * @return this builder
     */
    public Builder instrumentationProvider(String instrumentationProvider) {
      commonAttributesBuilder.instrumentationProvider(instrumentationProvider);
      return this;
    }

    /**
     * Builds the new MetricBuffer instance
     *
     * @return a newly created instance of MetricBuffer configured with data from this builder
     */
    public MetricBuffer build() {
      Attributes attributes = commonAttributesBuilder.build();
      return new MetricBuffer(attributes);
    }
  }
}
