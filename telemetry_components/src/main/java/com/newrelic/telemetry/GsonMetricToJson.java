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
import com.newrelic.telemetry.json.MetricToJson;
import java.io.IOException;
import java.io.StringWriter;

/**
 * This class generates JSON manually instead of serializing via GSON. The advantages of doing this
 * manually are to decouple the SDK types (e.g., Count, Gauge, Summary) from the API structure, and
 * to avoid excess object creation and allocation.
 */
public class GsonMetricToJson implements MetricToJson {

  private final Gson gson;
  private final AttributesGson attributeJson;

  static GsonMetricToJson build(Gson gson) {
    return new GsonMetricToJson(gson);
  }

  public GsonMetricToJson(Gson gson) {
    this.gson = new GsonBuilder().create();
    attributeJson = new AttributesGson(gson);
  }

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
      String attributes = attributeJson.toJson(summary.getAttributes());
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
      String attributes = attributeJson.toJson(gauge.getAttributes());
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

      String attributes = attributeJson.toJson(count.getAttributes());
      if (!attributes.isEmpty()) {
        jsonWriter.name("attributes").jsonValue(attributes);
      }
      jsonWriter.endObject();
      return out.toString();
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate count json");
    }
  }
}
