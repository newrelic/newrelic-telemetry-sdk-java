/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.spans.json;

import com.google.gson.stream.JsonWriter;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.spans.SpanBatch;
import java.io.IOException;

public class SpanJsonCommonBlockWriter {

  private final AttributesJson attributesJson;

  public SpanJsonCommonBlockWriter(AttributesJson attributesJson) {
    this.attributesJson = attributesJson;
  }

  public void appendCommonJson(SpanBatch batch, JsonWriter jsonWriter) {
    if (!batch.hasCommonAttributes() && !batch.getTraceId().isPresent()) {
      return;
    }
    try {
      jsonWriter.name("common");
      jsonWriter.beginObject();
      appendTraceId(batch, jsonWriter);
      appendAttributes(batch, jsonWriter);
      jsonWriter.endObject();
    } catch (IOException e) {
      throw new RuntimeException("Failed to create span common block json", e);
    }
  }

  private void appendTraceId(SpanBatch batch, JsonWriter jsonWriter) throws IOException {
    if (batch.getTraceId().isPresent()) {
      jsonWriter.name("trace.id").value(batch.getTraceId().get());
    }
  }

  private void appendAttributes(SpanBatch batch, JsonWriter jsonWriter) throws IOException {
    if (batch.hasCommonAttributes()) {
      jsonWriter.name("attributes");
      jsonWriter.jsonValue(attributesJson.toJson(batch.getCommonAttributes().asMap()));
    }
  }

  @Override
  public String toString() {
    return "SpanJsonCommonBlockWriter{" + "attributesJson=" + attributesJson + '}';
  }
}
