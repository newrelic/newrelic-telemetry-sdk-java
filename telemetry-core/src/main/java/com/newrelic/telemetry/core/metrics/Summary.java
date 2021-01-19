/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core.metrics;

import com.newrelic.telemetry.core.Attributes;
import com.newrelic.telemetry.core.util.Utils;
import java.util.Map;

/**
 * A {@link Metric} that represents an aggregated set of observations, with statistics, reported
 * over an interval.
 *
 * <p><b>Important</b>: Values are not validated on construction, and this class's methods do not
 * throw.
 */
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

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Summary summary = (Summary) o;

    if (getCount() != summary.getCount()) return false;
    if (Double.compare(summary.getSum(), getSum()) != 0) return false;
    if (Double.compare(summary.getMin(), getMin()) != 0) return false;
    if (Double.compare(summary.getMax(), getMax()) != 0) return false;
    if (getStartTimeMs() != summary.getStartTimeMs()) return false;
    if (getEndTimeMs() != summary.getEndTimeMs()) return false;
    if (getName() != null ? !getName().equals(summary.getName()) : summary.getName() != null)
      return false;
    return getAttributes() != null
        ? getAttributes().equals(summary.getAttributes())
        : summary.getAttributes() == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = getName() != null ? getName().hashCode() : 0;
    result = 31 * result + getCount();
    temp = Double.doubleToLongBits(getSum());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(getMin());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = Double.doubleToLongBits(getMax());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (int) (getStartTimeMs() ^ (getStartTimeMs() >>> 32));
    result = 31 * result + (int) (getEndTimeMs() ^ (getEndTimeMs() >>> 32));
    result = 31 * result + (getAttributes() != null ? getAttributes().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Summary{"
        + "name='"
        + name
        + '\''
        + ", count="
        + count
        + ", sum="
        + sum
        + ", min="
        + min
        + ", max="
        + max
        + ", startTimeMs="
        + startTimeMs
        + ", endTimeMs="
        + endTimeMs
        + ", attributes="
        + attributes
        + '}';
  }
}
