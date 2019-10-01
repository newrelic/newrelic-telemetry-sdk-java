/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.spans.json;

import static java.util.Optional.ofNullable;

import com.google.gson.stream.JsonWriter;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
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
        Map<String, Object> enhancedAttributes = new AttributesEnhancer(span).enhance();
        jsonWriter.name("attributes").jsonValue(attributesJson.toJson(enhancedAttributes));
        jsonWriter.endObject();
      }
      jsonWriter.endArray();
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate span telemetry json", e);
    }
  }

  private static class AttributesEnhancer {

    private final Span span;
    private final Map<String, Object> newAttrs;

    AttributesEnhancer(Span span) {
      this.span = span;
      this.newAttrs = new HashMap<>(span.getAttributes().asMap());
    }

    Map<String, Object> enhance() {
      put("name", () -> ofNullable(span.getName()));
      put("parent.id", () -> ofNullable(span.getParentId()));
      put("duration.ms", () -> ofNullable(span.getDurationMs()));
      put("service.name", () -> ofNullable(span.getServiceName()));
      if (span.isError()) {
        newAttrs.put("error", true);
      }
      return newAttrs;
    }

    private Object getAttr(String name) {
      return span.getAttributes().asMap().get(name);
    }

    private void put(String key, Supplier<Optional<Object>> sup1) {
      newAttrs.put(key, sup1.get().orElse(getAttr(key)));
    }

  }
}
