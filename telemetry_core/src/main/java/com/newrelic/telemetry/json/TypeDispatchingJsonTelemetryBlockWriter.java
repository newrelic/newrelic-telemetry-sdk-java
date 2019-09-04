package com.newrelic.telemetry.json;

import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.TelemetryBatch;

public class TypeDispatchingJsonTelemetryBlockWriter {

  private final JsonTelemetryBlockWriter mainBodyMetricsWriter;
  private final JsonTelemetryBlockWriter mainBodySpanWriter;

  public TypeDispatchingJsonTelemetryBlockWriter(
      JsonTelemetryBlockWriter mainBodyMetricsWriter, JsonTelemetryBlockWriter mainBodySpanWriter) {
    this.mainBodyMetricsWriter = mainBodyMetricsWriter;
    this.mainBodySpanWriter = mainBodySpanWriter;
  }

  public <T extends Telemetry> void appendTelemetryJson(
      TelemetryBatch<T> batch, StringBuilder builder) {
    chooseMainBodyWrite(batch).appendTelemetryJson(batch, builder);
  }

  private <T extends Telemetry> JsonTelemetryBlockWriter chooseMainBodyWrite(
      TelemetryBatch<T> batch) {
    switch (batch.getType()) {
      case METRIC:
        return mainBodyMetricsWriter;
      case SPAN:
        return mainBodySpanWriter;
    }
    throw new UnsupportedOperationException("Unhandled telemetry batch type: " + batch.getType());
  }
}
