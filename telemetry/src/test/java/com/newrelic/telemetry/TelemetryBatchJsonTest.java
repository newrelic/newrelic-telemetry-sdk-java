package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.newrelic.telemetry.json.JsonCommonBlockWriter;
import com.newrelic.telemetry.json.JsonTelemetryBlockWriter;
import com.newrelic.telemetry.json.TelemetryBatchJson;
import com.newrelic.telemetry.json.TypeDispatchingJsonCommonBlockWriter;
import com.newrelic.telemetry.json.TypeDispatchingJsonTelemetryBlockWriter;
import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class TelemetryBatchJsonTest {

  @Test
  void testJsonBuilding() {

    long now = System.currentTimeMillis();
    long later = now + 1500;
    Collection<Metric> metrics =
        Collections.singletonList(
            new Count("mmm", 12.1, now, later, new Attributes().put("f1", "b1")));
    TelemetryBatch<Metric> batch =
        TelemetryBatch.batchMetrics(metrics, new Attributes().put("c1", "d1"));

    String commonBit = "{\"common\": \"wad\"}";
    String telemetryBit = "{\"bunchOf\": \"telemetry\"}";

    JsonCommonBlockWriter commonWriter =
        new JsonCommonBlockWriter() {
          @Override
          public <T extends Telemetry> void appendCommonJson(
              TelemetryBatch<T> batch, StringBuilder builder) {
            builder.append(commonBit);
          }
        };
    JsonTelemetryBlockWriter mainBodyWriter =
        new JsonTelemetryBlockWriter() {
          @Override
          public <T extends Telemetry> void appendTelemetryJson(
              TelemetryBatch<T> batch, StringBuilder builder) {
            builder.append(telemetryBit);
          }
        };
    String expectedJson = "[{" + commonBit + "," + telemetryBit + "}]";

    TelemetryBatchJson testClass =
        new TelemetryBatchJson(
            new TypeDispatchingJsonCommonBlockWriter(commonWriter, null),
            new TypeDispatchingJsonTelemetryBlockWriter(mainBodyWriter, null));

    String result = testClass.toJson(batch);
    assertEquals(expectedJson, result);
  }
}
