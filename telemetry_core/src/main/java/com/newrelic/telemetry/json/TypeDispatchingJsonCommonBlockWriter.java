package com.newrelic.telemetry.json;

import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.TelemetryBatch;

public class TypeDispatchingJsonCommonBlockWriter<
    S extends Telemetry, T extends TelemetryBatch<S>> {

  private final JsonCommonBlockWriter<S, T> commonBlockMetricsWriter;
  private final JsonCommonBlockWriter<S, T> commonBlockSpanWriter;

  public TypeDispatchingJsonCommonBlockWriter(
      JsonCommonBlockWriter<S, T> commonBlockMetricsWriter,
      JsonCommonBlockWriter<S, T> commonBlockSpanWriter) {
    this.commonBlockMetricsWriter = commonBlockMetricsWriter;
    this.commonBlockSpanWriter = commonBlockSpanWriter;
  }

  public void appendCommonJson(T batch, StringBuilder builder) {
    chooseCommonWriter(batch).appendCommonJson(batch, builder);
  }

  private JsonCommonBlockWriter<S, T> chooseCommonWriter(T batch) {
    switch (batch.getType()) {
      case METRIC:
        return commonBlockMetricsWriter;
      case SPAN:
        return commonBlockSpanWriter;
    }
    throw new UnsupportedOperationException("Unhandled telemetry batch type: " + batch.getType());
  }
}
