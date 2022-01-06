package com.newrelic.telemetry.metrics.json;

import com.google.gson.stream.JsonWriter;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.Summary;
import java.io.IOException;
import java.io.StringWriter;

/**
 * This class turns Metrics into JSON via an embedded JsonWriter from the gson project. /** Note:
 * This is the same as MetricToJson.java, except all methods are static. This is for the
 * createBatches() method in the metric buffer. *
 */
public class MetricToJsonStatic {

  private static final AttributesJson attributeJson = new AttributesJson();

  public static String writeSummaryJson(Summary summary) {
    try {
      StringWriter out = new StringWriter();
      JsonWriter jsonWriter = new JsonWriter(out);
      jsonWriter.beginObject();
      jsonWriter.name("name").value(summary.getName());
      jsonWriter.name("type").value("summary");

      jsonWriter.name("value");
      jsonWriter.beginObject();
      jsonWriter.name("count").value(summary.getCount());
      jsonWriter.name("sum").value(summary.getSum());
      jsonWriter.name("min");
      writeDouble(jsonWriter, summary.getMin());
      jsonWriter.name("max");
      writeDouble(jsonWriter, summary.getMax());
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

  public static String writeGaugeJson(Gauge gauge) {
    try {
      StringWriter out = new StringWriter();
      JsonWriter jsonWriter = new JsonWriter(out);
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

  public static String writeCountJson(Count count) {
    try {
      StringWriter out = new StringWriter();
      JsonWriter jsonWriter = new JsonWriter(out);
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

  private static void writeDouble(final JsonWriter jsonWriter, final double value)
      throws IOException {
    if (Double.isFinite(value)) {
      jsonWriter.value(value);
    } else {
      jsonWriter.nullValue();
    }
  }
}
