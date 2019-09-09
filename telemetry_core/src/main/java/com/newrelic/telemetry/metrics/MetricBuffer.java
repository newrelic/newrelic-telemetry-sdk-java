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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
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
}
