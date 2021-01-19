/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.telemetry.core.logs;

import com.newrelic.telemetry.core.Attributes;
import com.newrelic.telemetry.core.TelemetryBatch;
import java.util.Collection;

/** Represents a set of {@link Log} instances, to be sent up to the New Relic Logging API. */
public class LogBatch extends TelemetryBatch<Log> {
  public LogBatch(Collection<Log> telemetry, Attributes commonAttributes) {
    super(telemetry, commonAttributes);
  }

  @Override
  public TelemetryBatch<Log> createSubBatch(Collection<Log> telemetry) {
    return new LogBatch(telemetry, getCommonAttributes());
  }
}
