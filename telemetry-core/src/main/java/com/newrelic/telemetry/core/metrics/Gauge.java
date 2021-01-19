/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core.metrics;

import com.newrelic.telemetry.core.Attributes;
import com.newrelic.telemetry.core.util.Utils;
import java.util.Map;

/**
 * A {@link Metric} representing a single signed, floating-point quantity measured at a point in
 * time.
 *
 * <p><b>Important</b>: Values are not validated on construction, and this class's methods do not
 * throw.
 */
public final class Gauge implements Metric {

  private final String name;
  private final double value;
  private final long timestamp;
  private final Map<String, Object> attributes;

  /**
   * @param name The name for this Gauge metric.
   * @param value The value of this Gauge, recorded at the point in time.
   * @param timestamp The point in time this Gauge measurement was recorded, in milliseconds since
   *     epoch.
   * @param attributes Dimensional attributes, as key-value pairs, associated with this Gauge.
   */
  public Gauge(String name, double value, long timestamp, Attributes attributes) {
    this.name = Utils.verifyNonNull(name);
    this.value = value;
    this.timestamp = timestamp;
    this.attributes = Utils.verifyNonNull(attributes).asMap();
  }

  /** @return The value of this Gauge, recorded at the point in time. */
  public double getValue() {
    return value;
  }

  /** @return The point in time this Gauge measurement was recorded, in milliseconds since epoch. */
  public long getTimestamp() {
    return timestamp;
  }

  /** @return The name for this Gauge metric. */
  public String getName() {
    return name;
  }

  /** @return Dimensional attributes, as key-value pairs, associated with this Gauge. */
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Gauge gauge = (Gauge) o;

    if (Double.compare(gauge.getValue(), getValue()) != 0) return false;
    if (getTimestamp() != gauge.getTimestamp()) return false;
    if (getName() != null ? !getName().equals(gauge.getName()) : gauge.getName() != null)
      return false;
    return getAttributes() != null
        ? getAttributes().equals(gauge.getAttributes())
        : gauge.getAttributes() == null;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = getName() != null ? getName().hashCode() : 0;
    temp = Double.doubleToLongBits(getValue());
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    result = 31 * result + (int) (getTimestamp() ^ (getTimestamp() >>> 32));
    result = 31 * result + (getAttributes() != null ? getAttributes().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Gauge{"
        + "name='"
        + name
        + '\''
        + ", value="
        + value
        + ", timestamp="
        + timestamp
        + ", attributes="
        + attributes
        + '}';
  }
}
