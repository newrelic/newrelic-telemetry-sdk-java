/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static java.util.stream.Collectors.toList;

import com.newrelic.telemetry.util.Utils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/** Represents a collection of {@link Telemetry} instances and some common attributes */
public abstract class TelemetryBatch<T extends Telemetry> {

  private Collection<T> telemetry;

  private Attributes commonAttributes;

  public TelemetryBatch(Collection<T> telemetry, Attributes commonAttributes) {
    this.telemetry = Utils.verifyNonNull(telemetry);
    this.commonAttributes = Utils.verifyNonNull(commonAttributes);
  }

  /**
   * Split this batch into 2 roughly equal pieces. If the initial batch contains no telemetry, this
   * will simply return an empty list of batches.
   *
   * @return a List of telemetry batches, roughly split in 2.
   */
  public List<TelemetryBatch<T>> split() {
    if (telemetry.isEmpty()) {
      return Collections.emptyList();
    }
    int totalSize = telemetry.size();
    int halfSize = totalSize / 2;

    return Arrays.asList(
        createSubBatch(telemetry.stream().limit(halfSize).collect(toList())),
        createSubBatch(telemetry.stream().skip(halfSize).collect(toList())));
  }

  /**
   * Returns the number of telemetry items in this collection. If this batch contains more than
   * {Integer.MAX_VALUE} items, returns {Integer.MAX_VALUE}.
   *
   * @return the number of telemetry items in this batch
   */
  public int size() {
    return telemetry.size();
  }

  /** @return true if the common attributes are not empty */
  public boolean hasCommonAttributes() {
    return !commonAttributes.isEmpty();
  }

  public boolean isEmpty() {
    return telemetry.isEmpty();
  }

  public abstract TelemetryBatch<T> createSubBatch(Collection<T> telemetry);

  public Collection<T> getTelemetry() {
    return telemetry;
  }

  public Attributes getCommonAttributes() {
    return commonAttributes;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    TelemetryBatch<?> that = (TelemetryBatch<?>) o;

    if (getTelemetry() != null
        ? !getTelemetry().equals(that.getTelemetry())
        : that.getTelemetry() != null) return false;
    return getCommonAttributes() != null
        ? getCommonAttributes().equals(that.getCommonAttributes())
        : that.getCommonAttributes() == null;
  }

  @Override
  public int hashCode() {
    int result = getTelemetry() != null ? getTelemetry().hashCode() : 0;
    result = 31 * result + (getCommonAttributes() != null ? getCommonAttributes().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "TelemetryBatch{"
        + "telemetry="
        + telemetry
        + ", commonAttributes="
        + commonAttributes
        + '}';
  }
}
