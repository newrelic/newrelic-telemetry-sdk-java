/*
 * ---------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.json;

import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.TelemetryBatch;

/**
 * Implementations of this class are responsible for converting a telemetry batch into JSON
 * fragments. This particular writer is responsible for writing the block of a batch that contains
 * telemetry.
 */
public interface JsonTelemetryBlockWriter<S extends Telemetry, T extends TelemetryBatch<S>> {

  void appendTelemetryJson(T batch, StringBuilder builder);
}
