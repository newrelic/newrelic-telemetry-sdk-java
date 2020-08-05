/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.util.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
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

  private final Attributes commonAttributes;

  /**
   * Create a new buffer with the provided common set of attributes.
   *
   * @param commonAttributes These attributes will be appended (by the New Relic backend) to every
   *     {@link Metric} in this buffer.
   */
  public MetricBuffer(Attributes commonAttributes) {
    this.commonAttributes = Utils.verifyNonNull(commonAttributes);
  }

  /**
   * Append a {@link Metric} to this buffer, to be sent in the next {@link MetricBatch}.
   *
   * @param metric The new {@link Metric} instance to be sent.
   */
  public void addMetric(Metric metric) {
    metrics.add(metric);
  }

  /**
   * Get the size of the {@link Metric Metrics} buffer.
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
  public MetricBatch createBatch() {
    logger.debug("Creating metric batch.");
    Collection<Metric> metrics = new ArrayList<>(this.metrics.size());

    // Drain the metric buffer and return the batch
    Metric metric;
    while ((metric = this.metrics.poll()) != null) {
      metrics.add(metric);
    }

    return new MetricBatch(metrics, this.commonAttributes);
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
