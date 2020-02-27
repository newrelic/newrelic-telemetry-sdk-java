/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.util.Utils;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A buffer for collecting {@link Metric Metrics}.
 *
 * <p>One instance of this class can collect many {@link Metric Metrics}. To send them to the
 * Metrics API, call {@link #createBatch()} and then {@link
 * MetricBatchSender#sendBatch(MetricBatch)}.
 *
 * <p>This class is thread-safe.
 */
@Value
public class MetricBuffer {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MetricBuffer.class);

  @Getter(AccessLevel.PACKAGE)
  private final Queue<Metric> metrics = new ConcurrentLinkedQueue<>();

  @Getter(AccessLevel.PACKAGE)
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

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private Attributes attributes = new Attributes();
    private String serviceName;
    private String instrumentationProvider;

    public Builder attributes(Attributes attributes) {
      this.attributes = attributes;
      return this;
    }

    /**
     * Optional. Specify the name of the service that is creating the metrics. The service name will
     * be included in all common attributes.
     *
     * @param serviceName - The name of the service
     */
    public Builder serviceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    /**
     * Optional. Specify the name of the instrumentation that provides the metrics. The
     * instrumentation provider will be included in all common attributes.
     *
     * @param instrumentationProvider - The instrumentation provider name
     */
    public Builder instrumentationProvider(String instrumentationProvider) {
      this.instrumentationProvider = instrumentationProvider;
      return this;
    }

    public MetricBuffer build() {
      Attributes attributes = new Attributes(this.attributes);
      if (serviceName != null) {
        attributes.put("service.name", serviceName);
      }
      if (instrumentationProvider != null) {
        attributes.put("instrumentation.provider", instrumentationProvider);
      }
      return new MetricBuffer(attributes);
    }
  }
}
