/*
 * ---------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.newrelic.telemetry.json.JsonCommonBlockWriter;
import com.newrelic.telemetry.json.JsonTelemetryBlockWriter;
import com.newrelic.telemetry.json.TelemetryBatchJson;
import com.newrelic.telemetry.json.TypeDispatchingJsonCommonBlockWriter;
import com.newrelic.telemetry.json.TypeDispatchingJsonTelemetryBlockWriter;
import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.Metric;
import com.newrelic.telemetry.metrics.MetricBatch;
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
    MetricBatch batch = TelemetryBatch.batchMetrics(metrics, new Attributes().put("c1", "d1"));

    String commonBit = "{\"common\": \"wad\"}";
    String telemetryBit = "{\"bunchOf\": \"telemetry\"}";

    JsonCommonBlockWriter<Metric, MetricBatch> commonWriter =
        (batch1, builder) -> builder.append(commonBit);
    JsonTelemetryBlockWriter<Metric, MetricBatch> mainBodyWriter =
        (batch12, builder) -> builder.append(telemetryBit);
    String expectedJson = "[{" + commonBit + "," + telemetryBit + "}]";

    TelemetryBatchJson testClass =
        new TelemetryBatchJson(
            new TypeDispatchingJsonCommonBlockWriter(commonWriter, null),
            new TypeDispatchingJsonTelemetryBlockWriter(mainBodyWriter, null));

    String result = testClass.toJson(batch);
    assertEquals(expectedJson, result);
  }
}
