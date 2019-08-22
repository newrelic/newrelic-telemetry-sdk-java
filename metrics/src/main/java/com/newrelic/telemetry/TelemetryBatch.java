/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import static java.util.stream.Collectors.toList;

import com.newrelic.telemetry.Telemetry.Type;
import com.newrelic.telemetry.util.Utils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import lombok.experimental.NonFinal;

/** Represents a collection of {@link Telemetry} instances and some common attributes */
@Value
@NonFinal
public class TelemetryBatch<T extends Telemetry> {

  private final Type type;

  @Getter(AccessLevel.PACKAGE)
  Collection<T> telemetry;

  @Getter(AccessLevel.PACKAGE)
  Attributes commonAttributes;

  public TelemetryBatch(Telemetry.Type type, Collection<T> telemetry, Attributes commonAttributes) {
    this.type = type;
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
        new TelemetryBatch<>(
            type, telemetry.stream().limit(halfSize).collect(toList()), commonAttributes),
        new TelemetryBatch<>(
            type, telemetry.stream().skip(halfSize).collect(toList()), commonAttributes));
  }

  /**
   * Returns the number of telemetry items in this collection. If this batch contains more than
   * <tt>Integer.MAX_VALUE</tt> items, returns <tt>Integer.MAX_VALUE</tt>.
   *
   * @return the number of telemetry items in this batch
   */
  public int size() {
    return telemetry.size();
  }
}
