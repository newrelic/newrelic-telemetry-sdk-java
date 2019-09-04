package com.newrelic.telemetry.json;

import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.TelemetryBatch;

public class TypeDispatchingJsonTelemetryBlockWriter<
    S extends Telemetry, T extends TelemetryBatch<S>> {

  private final JsonTelemetryBlockWriter<S, T> mainBodyMetricsWriter;
  private final JsonTelemetryBlockWriter<S, T> mainBodySpanWriter;

  public TypeDispatchingJsonTelemetryBlockWriter(
      JsonTelemetryBlockWriter<S, T> mainBodyMetricsWriter,
      JsonTelemetryBlockWriter<S, T> mainBodySpanWriter) {
    this.mainBodyMetricsWriter = mainBodyMetricsWriter;
    this.mainBodySpanWriter = mainBodySpanWriter;
  }

  public void appendTelemetryJson(T batch, StringBuilder builder) {
    chooseMainBodyWrite(batch).appendTelemetryJson(batch, builder);
  }

  private JsonTelemetryBlockWriter<S, T> chooseMainBodyWrite(T batch) {
    switch (batch.getType()) {
      case METRIC:
        return mainBodyMetricsWriter;
      case SPAN:
        return mainBodySpanWriter;
    }
    throw new UnsupportedOperationException("Unhandled telemetry batch type: " + batch.getType());
  }
}
