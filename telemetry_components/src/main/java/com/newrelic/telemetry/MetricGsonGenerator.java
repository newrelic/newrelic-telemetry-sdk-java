/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * This class generates JSON manually instead of serializing via GSON. The advantages of doing this
 * manually are to decouple the SDK types (e.g., Count, Gauge, Summary) from the API structure, and
 * to avoid excess object creation and allocation.
 */
public class MetricGsonGenerator implements MetricJsonGenerator {

  private final Gson gson = new GsonBuilder().create();

  @Override
  public String writeSummaryJson(Summary summary) {
    try {
      StringWriter out = new StringWriter();
      JsonWriter jsonWriter = gson.newJsonWriter(out);
      jsonWriter.beginObject();
      jsonWriter.name("name").value(summary.getName());
      jsonWriter.name("type").value("summary");

      jsonWriter.name("value");
      jsonWriter.beginObject();
      jsonWriter.name("count").value(summary.getCount());
      jsonWriter.name("sum").value(summary.getSum());
      jsonWriter.name("min").value(summary.getMin());
      jsonWriter.name("max").value(summary.getMax());
      jsonWriter.endObject();

      jsonWriter.name("timestamp").value(summary.getStartTimeMs());
      jsonWriter.name("interval.ms").value(summary.getEndTimeMs() - summary.getStartTimeMs());
      String attributes = writeAttributes(summary.getAttributes());
      if (!attributes.isEmpty()) {
        jsonWriter.name("attributes").jsonValue(attributes);
      }
      jsonWriter.endObject();
      return out.toString();
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate summary json", e);
    }
  }

  @Override
  public String writeGaugeJson(Gauge gauge) {
    try {
      StringWriter out = new StringWriter();
      JsonWriter jsonWriter = gson.newJsonWriter(out);
      jsonWriter.beginObject();
      jsonWriter.name("name").value(gauge.getName());
      jsonWriter.name("type").value("gauge");
      jsonWriter.name("value").value(gauge.getValue());
      jsonWriter.name("timestamp").value(gauge.getTimestamp());
      String attributes = writeAttributes(gauge.getAttributes());
      if (!attributes.isEmpty()) {
        jsonWriter.name("attributes").jsonValue(attributes);
      }
      jsonWriter.endObject();
      return out.toString();
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate gauge json", e);
    }
  }

  @Override
  public String writeCountJson(Count count) {
    try {
      StringWriter out = new StringWriter();
      JsonWriter jsonWriter = gson.newJsonWriter(out);
      jsonWriter.beginObject();
      jsonWriter.name("name").value(count.getName());
      jsonWriter.name("type").value("count");
      jsonWriter.name("value").value(count.getValue());
      jsonWriter.name("timestamp").value(count.getStartTimeMs());
      jsonWriter.name("interval.ms").value(count.getEndTimeMs() - count.getStartTimeMs());

      String attributes = writeAttributes(count.getAttributes());
      if (!attributes.isEmpty()) {
        jsonWriter.name("attributes").jsonValue(attributes);
      }
      jsonWriter.endObject();
      return out.toString();
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate count json");
    }
  }

  @Override
  public String writeAttributes(Map<String, Object> attributes) {
    StringWriter out = new StringWriter();
    Map<String, Object> filteredAttributes = filterIllegalValues(attributes);
    filterIllegalValues(filteredAttributes);
    if (filteredAttributes.isEmpty()) {
      return "{}";
    }
    try {
      JsonWriter jsonWriter = gson.newJsonWriter(out);
      jsonWriter.beginObject();

      for (Map.Entry<String, Object> attribute : filteredAttributes.entrySet()) {
        Object value = attribute.getValue();

        if (value instanceof Boolean) {
          jsonWriter.name(attribute.getKey()).value((boolean) value);
        } else if (value instanceof Number) {
          Number num = (Number) value;
          jsonWriter.name(attribute.getKey()).value(num);
        } else {
          jsonWriter.name(attribute.getKey()).value(String.valueOf(value));
        }
      }
      jsonWriter.endObject();
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate attributes json");
    }
    return out.toString();
  }
}
