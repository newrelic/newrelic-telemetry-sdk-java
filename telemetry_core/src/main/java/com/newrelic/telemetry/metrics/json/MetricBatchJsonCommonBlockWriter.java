package com.newrelic.telemetry.metrics.json;

import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.json.JsonCommonBlockWriter;
import com.newrelic.telemetry.metrics.Metric;
import com.newrelic.telemetry.metrics.MetricBatch;

public class MetricBatchJsonCommonBlockWriter
    implements JsonCommonBlockWriter<Metric, MetricBatch> {

  private final AttributesJson attributesJson;

  public MetricBatchJsonCommonBlockWriter(AttributesJson attributesJson) {
    this.attributesJson = attributesJson;
  }

  @Override
  public void appendCommonJson(MetricBatch batch, StringBuilder builder) {
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
