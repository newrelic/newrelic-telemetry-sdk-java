/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import static java.util.stream.Collectors.toList;

import com.newrelic.telemetry.model.Attributes;
import com.newrelic.telemetry.util.Utils;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;

/** Represents a set of {@link Metric} instances, to be sent up to the New Relic Metrics API. */
@Value
public class MetricBatch {

  @Getter(AccessLevel.PACKAGE)
  Collection<Metric> metrics;

  @Getter(AccessLevel.PACKAGE)
  Attributes commonAttributes;

  public MetricBatch(Collection<Metric> metrics, Attributes commonAttributes) {
    this.metrics = Utils.verifyNonNull(metrics);
    this.commonAttributes = Utils.verifyNonNull(commonAttributes);
  }

  /**
   * Split this batch into 2 roughly equal pieces. If the initial batch contains no metrics, this
   * will simply return an empty list of batches.
   *
   * @return a List of metric batches, roughly split in 2.
   */
  public List<MetricBatch> split() {
    if (metrics.isEmpty()) {
      return Collections.emptyList();
    }
    int totalSize = metrics.size();
    int halfSize = totalSize / 2;

    return Arrays.asList(
        new MetricBatch(metrics.stream().limit(halfSize).collect(toList()), commonAttributes),
        new MetricBatch(metrics.stream().skip(halfSize).collect(toList()), commonAttributes));
  }

  /**
   * Returns the number of metrics in this collection. If this batch contains more than
   * <tt>Integer.MAX_VALUE</tt> metrics, returns <tt>Integer.MAX_VALUE</tt>.
   *
   * @return the number of metrics in this batch
   */
  public int size() {
    return metrics.size();
  }
}
