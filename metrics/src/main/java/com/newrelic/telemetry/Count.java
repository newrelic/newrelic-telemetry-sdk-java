/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import com.newrelic.telemetry.util.Utils;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A {@link Metric} that represents a single signed, floating-point quantity measured over a period
 * of time.
 *
 * <p><b>Important</b>: Values are not validated on construction, and this class's methods do not
 * throw.
 */
@ToString
@EqualsAndHashCode
public final class Count implements Metric {

  private final String name;
  private final double value;
  private final long startTimeMs;
  private final long endTimeMs;
  private final Map<String, Object> attributes;

  /**
   * Create a new Count instance.
   *
   * @param name The name of this count instance.
   * @param value The value for this count instance.
   * @param startTimeMs The start time of the interval over which the count was measured. This is in
   *     milliseconds since epoch.
   * @param endTimeMs The end time of the interval over which the count was measured. This is in
   *     milliseconds since epoch.
   * @param attributes Any additional attributes associated with this count.
   */
  public Count(String name, double value, long startTimeMs, long endTimeMs, Attributes attributes) {
    this.name = Utils.verifyNonNull(name);
    this.value = value;
    this.startTimeMs = startTimeMs;
    this.endTimeMs = endTimeMs;
    this.attributes = Utils.verifyNonNull(attributes).asMap();
  }

  /** @return The name for this Count metric. */
  public String getName() {
    return name;
  }

  /** @return Dimensional attributes, as key-value pairs, associated with this Count. */
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  /**
   * @return The value, which should be a positive number, representing the Count for the provided
   *     interval.
   */
  public double getValue() {
    return value;
  }

  /**
   * @return The start time of the interval over which this Count was recorded, in milliseconds
   *     since epoch.
   */
  public long getStartTimeMs() {
    return startTimeMs;
  }

  /**
   * @return The end time of the interval over which this Count was recorded, in milliseconds since
   *     epoch.
   */
  public long getEndTimeMs() {
    return endTimeMs;
  }
}
