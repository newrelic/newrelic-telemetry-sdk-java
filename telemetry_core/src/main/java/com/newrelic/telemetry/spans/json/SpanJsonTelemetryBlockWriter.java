package com.newrelic.telemetry.spans.json;

import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.json.JsonTelemetryBlockWriter;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Value;

@Value
public class SpanJsonTelemetryBlockWriter implements JsonTelemetryBlockWriter<Span, SpanBatch> {

  private final AttributesJson attributesJson;

  @Override
  public void appendTelemetryJson(SpanBatch batch, StringBuilder sb) {
    AtomicBoolean isFirst = new AtomicBoolean(true);
    sb.append("\"spans\":[");
    batch
        .getTelemetry()
        .forEach(
            span -> {
              if (!isFirst.getAndSet(false)) {
                sb.append(",");
              }
              sb.append("{")
                  .append("\"id\":\"")
                  .append(span.getId())
                  .append("\",")
                  .append("\"trace.id\":\"")
                  .append(span.getTraceId())
                  .append("\",")
                  .append("\"timestamp\":")
                  .append(span.getTimestamp())
                  .append(",")
                  .append("\"attributes\":" + attributesJson.toJson(enhanceAttributes(span)))
                  .append("}");
            });
    sb.append("]");
  }

  private Map<String, Object> enhanceAttributes(Span span) {
    Map<String, Object> result = new HashMap<>(span.getAttributes().asMap());
    result.put("name", span.getName());
    result.put("parent.id", span.getParentId());
    result.put("duration.ms", span.getDurationMs());
    result.put("service.name", span.getServiceName());
    return result;
  }
}
