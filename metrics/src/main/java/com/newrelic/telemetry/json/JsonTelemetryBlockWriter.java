package com.newrelic.telemetry.json;

import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.TelemetryBatch;

public interface JsonTelemetryBlockWriter {

  <T extends Telemetry> void appendTelemetry(TelemetryBatch<T> batch, StringBuilder builder);
}
