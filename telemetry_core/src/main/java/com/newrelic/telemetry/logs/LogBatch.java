/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.telemetry.logs;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.TelemetryBatch;
import java.util.Collection;

public class LogBatch extends TelemetryBatch<Log> {
  public LogBatch(Collection<Log> telemetry, Attributes commonAttributes) {
    super(telemetry, commonAttributes);
  }

  @Override
  public TelemetryBatch<Log> createSubBatch(Collection<Log> telemetry) {
    return new LogBatch(telemetry, getCommonAttributes());
  }
}
