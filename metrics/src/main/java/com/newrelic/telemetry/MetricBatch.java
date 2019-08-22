/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import com.newrelic.telemetry.Telemetry.Type;
import java.util.Collection;
import lombok.Value;

/** Represents a set of {@link Metric} instances, to be sent up to the New Relic Metrics API. */
@Value
public class MetricBatch extends TelemetryBatch<Metric> {

  public MetricBatch(Collection<Metric> metrics, Attributes commonAttributes) {
    super(Type.METRIC, metrics, commonAttributes);
  }
}
