/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.spans.json;

import com.google.gson.stream.JsonWriter;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class SpanJsonTelemetryBlockWriter {

  private final AttributesJson attributesJson;

  public SpanJsonTelemetryBlockWriter(AttributesJson attributesJson) {
    this.attributesJson = attributesJson;
  }

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
    result.putIfAbsent("name", span.getName());
    result.putIfAbsent("parent.id", span.getParentId());
    result.putIfAbsent("duration.ms", span.getDurationMs());
    result.putIfAbsent("service.name", span.getServiceName());
    if (span.isError()) {
      result.put("error", true);
    }
    return result;
  }

  public AttributesJson getAttributesJson() {
    return attributesJson;
  }

  @Override
  public String toString() {
    return "SpanJsonTelemetryBlockWriter{" + "attributesJson=" + attributesJson + '}';
  }
}
