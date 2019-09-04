package com.newrelic.telemetry.spans.json;

import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.json.JsonTelemetryBlockWriter;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Value;

@Value
public class SpanJsonTelemetryBlockWriter implements JsonTelemetryBlockWriter<Span, SpanBatch> {

  private final AttributesJson attributesJson;

  @Override
  public void appendTelemetryJson(SpanBatch batch, StringBuilder sb) {
    AtomicBoolean isFirst = new AtomicBoolean(true);
    sb.append("\"spans\":[");
    batch.getTelemetry().forEach(span -> {
      if(!isFirst.getAndSet(false)){
        sb.append(",");
      }
      sb.append("{")
          .append("\"id\":\"")
          .append(span.getId())
          .append("\",")
          .append(attributesJson.toJson(span.getAttributes().asMap()))
          .append("}");
    });
    sb.append("]");
  }
}
