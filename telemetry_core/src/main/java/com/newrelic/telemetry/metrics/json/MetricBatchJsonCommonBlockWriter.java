package com.newrelic.telemetry.metrics.json;

import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.TelemetryBatch;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.json.JsonCommonBlockWriter;

public class MetricBatchJsonCommonBlockWriter implements JsonCommonBlockWriter {

  private final AttributesJson attributesJson;

  public MetricBatchJsonCommonBlockWriter(AttributesJson attributesJson) {
    this.attributesJson = attributesJson;
  }

  @Override
  public <T extends Telemetry> void appendCommonJson(
      TelemetryBatch<T> batch, StringBuilder builder) {
    if (batch.hasCommonAttributes()) {
      builder
          .append("\"common\":")
          .append("{")
          .append("\"attributes\":")
          .append(attributesJson.toJson(batch.getCommonAttributes().asMap()))
          .append("}");
    }
  }
}
