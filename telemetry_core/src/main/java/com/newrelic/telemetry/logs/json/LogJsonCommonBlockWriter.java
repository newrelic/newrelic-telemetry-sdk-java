/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.logs.json;

import com.google.gson.stream.JsonWriter;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.logs.LogBatch;
import java.io.IOException;

public class LogJsonCommonBlockWriter {

  private final AttributesJson attributesJson;

  public LogJsonCommonBlockWriter(AttributesJson attributesJson) {
    this.attributesJson = attributesJson;
  }

  public void appendCommonJson(LogBatch batch, JsonWriter jsonWriter) {
    if (!batch.hasCommonAttributes()) {
      return;
    }
    try {
      jsonWriter.name("common");
      jsonWriter.beginObject();
      jsonWriter.name("attributes");
      jsonWriter.jsonValue(attributesJson.toJson(batch.getCommonAttributes().asMap()));
      jsonWriter.endObject();
    } catch (IOException e) {
      throw new RuntimeException("Failed to create span common block json", e);
    }
  }

  @Override
  public String toString() {
    return "LogJsonCommonBlockWriter{" + "attributesJson=" + attributesJson + '}';
  }
}
