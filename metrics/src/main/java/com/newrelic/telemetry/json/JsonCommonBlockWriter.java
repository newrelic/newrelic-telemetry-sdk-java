package com.newrelic.telemetry.json;

import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.TelemetryBatch;

public interface JsonCommonBlockWriter {

  <T extends Telemetry> void appendCommonJson(TelemetryBatch<T> batch, StringBuilder builder);
}
