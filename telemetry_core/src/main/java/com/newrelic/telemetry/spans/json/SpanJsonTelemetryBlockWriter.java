/*
 * ---------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.spans.json;

import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.json.JsonWriter;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Value;

@Value
public class SpanJsonTelemetryBlockWriter {

  private final AttributesJson attributesJson;

  public void appendTelemetryJson(SpanBatch batch, JsonWriter jsonWriter) {
    try {
      jsonWriter.name("spans");
      jsonWriter.beginArray();
      Collection<Span> telemetry = batch.getTelemetry();
      for (Span span : telemetry) {
        jsonWriter.beginObject();
        jsonWriter.name("id").value(span.getId());
        if (span.getTraceId() != null) {
          jsonWriter.name("trace.id").value(span.getTraceId());
        }
        jsonWriter.name("timestamp").value(span.getTimestamp());
        jsonWriter.name("attributes").jsonValue(attributesJson.toJson(enhanceAttributes(span)));
        jsonWriter.endObject();
      }
      jsonWriter.endArray();
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate span telemetry json", e);
    }
  }

  private Map<String, Object> enhanceAttributes(Span span) {
    Map<String, Object> result = new HashMap<>(span.getAttributes().asMap());
    result.put("name", span.getName());
    result.put("parent.id", span.getParentId());
    result.put("duration.ms", span.getDurationMs());
    result.put("service.name", span.getServiceName());
    if (span.isError()) {
      result.put("error", true);
    }
    return result;
  }
}
