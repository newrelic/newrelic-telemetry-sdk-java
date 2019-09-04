package com.newrelic.telemetry.json;

import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.TelemetryBatch;

/**
 * Implementations of this class are responsible for converting a telemetry batch into JSON
 * fragments. This particular writer is responsible for writing the "common" block for a single
 * batch.
 */
public interface JsonCommonBlockWriter<S extends Telemetry, T extends TelemetryBatch<S>> {

  void appendCommonJson(T batch, StringBuilder builder);
}
