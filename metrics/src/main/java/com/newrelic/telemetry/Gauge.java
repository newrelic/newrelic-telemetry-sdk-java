/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import com.newrelic.telemetry.model.Attributes;
import com.newrelic.telemetry.util.Utils;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A {@link Metric} representing a single signed, floating-point quantity measured at a point in
 * time.
 *
 * <p><b>Important</b>: Values are not validated on construction, and this class's methods do not
 * throw.
 */
@ToString
@EqualsAndHashCode
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
}
