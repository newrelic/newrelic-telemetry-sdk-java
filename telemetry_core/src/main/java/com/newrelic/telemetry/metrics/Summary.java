/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.util.Utils;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A {@link Metric} that represents an aggregated set of observations, with statistics, reported
 * over an interval.
 *
 * <p><b>Important</b>: Values are not validated on construction, and this class's methods do not
 * throw.
 */
@ToString
@EqualsAndHashCode
public final class Summary implements Metric {

  private final String name;
  private final int count;
  private final double sum;
  private final double min;
  private final double max;
  private final long startTimeMs;
  private final long endTimeMs;
  private final Map<String, Object> attributes;

  /**
   * Create a new instance of a summary metric.
   *
   * @param name The name for this Summary metric.
   * @param count The number of observations that are aggregated by this Summary.
   * @param sum The total of the aggregated observations.
   * @param min The minimum of the aggregated observations.
   * @param max The maximum of the aggregated observations.
   * @param startTimeMs The start time for the interval over which this Summary applies, in
   *     milliseconds since * epoch.
   * @param endTimeMs The end time for the interval over which this Summary applies, in milliseconds
   *     since * epoch.
   * @param attributes Dimensional attributes, as key-value pairs, associated with this instance.
   */
  public Summary(
      String name,
      int count,
      double sum,
      double min,
      double max,
      long startTimeMs,
      long endTimeMs,
      Attributes attributes) {
    this.name = Utils.verifyNonNull(name);
    this.count = count;
    this.sum = sum;
    this.min = min;
    this.max = max;
    this.startTimeMs = startTimeMs;
    this.endTimeMs = endTimeMs;
    this.attributes = Utils.verifyNonNull(attributes).asMap();
  }

  /**
   * Gets the user-visible name of the Metric.
   *
   * @return The user-visible name of the Metric.
   */
  public String getName() {
    return name;
  }

  /**
   * Gets dimensional attributes associated with this Metric.
   *
   * @return The dimensional attributes associated with this Metric.
   */
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  /** @return The number of observations that are aggregated by this Summary. */
  public int getCount() {
    return count;
  }

  /** @return The total of the aggregated observations. */
  public double getSum() {
    return sum;
  }

  /** @return The minimum of the aggregated observations. */
  public double getMin() {
    return min;
  }

  /** @return The maximum of the aggregated observations. */
  public double getMax() {
    return max;
  }

  /**
   * @return The start time for the interval over which this Summary applies, in milliseconds since
   *     epoch.
   */
  public long getStartTimeMs() {
    return startTimeMs;
  }

  /**
   * @return The end time for the interval over which this Summary applies, in milliseconds since
   *     epoch.
   */
  public long getEndTimeMs() {
    return endTimeMs;
  }
}
