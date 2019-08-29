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
 * This class turns Metrics into JSON via GSON.
 */
public class MetricToGson implements MetricToJson {

  private final Gson gson;
  private final AttributesGson attributeJson;

  static MetricToGson build(Gson gson) {
    return new MetricToGson(gson);
  }

  public MetricToGson(Gson gson) {
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
