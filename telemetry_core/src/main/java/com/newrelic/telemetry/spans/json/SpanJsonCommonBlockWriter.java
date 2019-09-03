package com.newrelic.telemetry.spans.json;


import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.TelemetryBatch;
import com.newrelic.telemetry.json.JsonCommonBlockWriter;

public class SpanJsonCommonBlockWriter implements JsonCommonBlockWriter {

  @Override
  public <T extends Telemetry> void appendCommonJson(TelemetryBatch<T> batch,
      StringBuilder builder) {

  }
}
