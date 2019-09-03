package com.newrelic.telemetry.json;

import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.TelemetryBatch;

public class TypeDispatchingJsonCommonBlockWriter implements JsonCommonBlockWriter {

  private final JsonCommonBlockWriter commonBlockMetricsWriter;
  private final JsonCommonBlockWriter commonBlockSpanWriter;

  public TypeDispatchingJsonCommonBlockWriter(
      JsonCommonBlockWriter commonBlockMetricsWriter, JsonCommonBlockWriter commonBlockSpanWriter) {
    this.commonBlockMetricsWriter = commonBlockMetricsWriter;
    this.commonBlockSpanWriter = commonBlockSpanWriter;
  }

  @Override
  public <T extends Telemetry> void appendCommonJson(
      TelemetryBatch<T> batch, StringBuilder builder) {
    chooseCommonWriter(batch).appendCommonJson(batch, builder);
  }

  private <T extends Telemetry> JsonCommonBlockWriter chooseCommonWriter(TelemetryBatch<T> batch) {
    switch (batch.getType()) {
      case METRIC:
        return commonBlockMetricsWriter;
      case SPAN:
        return commonBlockSpanWriter;
    }
    throw new UnsupportedOperationException("Unhandled telemetry batch type: " + batch.getType());
  }
}
