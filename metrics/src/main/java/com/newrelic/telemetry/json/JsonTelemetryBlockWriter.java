package com.newrelic.telemetry.json;

import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.TelemetryBatch;

/**
 * Implementations of this class are responsible for converting a telemetry batch into JSON
 * fragments. This particular writer is responsible for writing the block of a batch that contains
 * telemetry.
 */
public interface JsonTelemetryBlockWriter {

  <T extends Telemetry> void appendTelemetryJson(TelemetryBatch<T> batch, StringBuilder builder);
}
