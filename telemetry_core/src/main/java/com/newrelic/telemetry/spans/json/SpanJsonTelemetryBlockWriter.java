/*
 * ---------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.spans.json;

import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Value;

@Value
public class SpanJsonTelemetryBlockWriter {

  private final AttributesJson attributesJson;

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
              sb.append("{").append("\"id\":\"").append(span.getId()).append("\",");
              appendIfTraceIdExists(sb, span)
                  .append("\"timestamp\":")
                  .append(span.getTimestamp())
                  .append(",")
                  .append("\"attributes\":" + attributesJson.toJson(enhanceAttributes(span)))
                  .append("}");
            });
    sb.append("]");
  }

  private StringBuilder appendIfTraceIdExists(StringBuilder sb, Span span) {
    if (span.getTraceId() != null) {
      sb.append("\"trace.id\":\"").append(span.getTraceId()).append("\",");
    }
    return sb;
  }

  private Map<String, Object> enhanceAttributes(Span span) {
    Map<String, Object> result = new HashMap<>(span.getAttributes().asMap());
    result.put("name", span.getName());
    result.put("parent.id", span.getParentId());
    result.put("duration.ms", span.getDurationMs());
    result.put("service.name", span.getServiceName());
    if(span.isError()){
      result.put("error", true);
    }
    return result;
  }
}
