/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.logs.json;

import com.google.gson.stream.JsonWriter;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.logs.Log;
import com.newrelic.telemetry.logs.LogBatch;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class LogJsonTelemetryBlockWriter {

  private final AttributesJson attributesJson;

  public LogJsonTelemetryBlockWriter(AttributesJson attributesJson) {
    this.attributesJson = attributesJson;
  }

  public void appendTelemetryJson(LogBatch batch, JsonWriter jsonWriter) {
    try {
      jsonWriter.name("logs");
      jsonWriter.beginArray();
      Collection<Log> telemetry = batch.getTelemetry();
      for (Log log : telemetry) {
        jsonWriter.beginObject();
        jsonWriter.name("timestamp").value(log.getTimestamp());
        jsonWriter.name("attributes").jsonValue(attributesJson.toJson(enhanceAttributes(log)));
        jsonWriter.name("message").value(log.getMessage());
        jsonWriter.endObject();
      }
      jsonWriter.endArray();
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate span telemetry json", e);
    }
  }

  private Map<String, Object> enhanceAttributes(Log log) {
    Map<String, Object> result = new HashMap<>(log.getAttributes().asMap());
    result.putIfAbsent("service.name", log.getServiceName());
    if (log.getLevel() != null) {
      result.put("level", log.getLevel());
    }
    if (log.getLogType() != null) {
      result.put("logtype", log.getLogType());
    }
    return result;
  }

  public AttributesJson getAttributesJson() {
    return attributesJson;
  }

  @Override
  public String toString() {
    return "LogJsonTelemetryBlockWriter{" + "attributesJson=" + attributesJson + '}';
  }
}
