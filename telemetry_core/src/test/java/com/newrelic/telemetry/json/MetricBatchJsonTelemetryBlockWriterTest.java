/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.Metric;
import com.newrelic.telemetry.metrics.MetricBatch;
import com.newrelic.telemetry.metrics.json.MetricBatchJsonTelemetryBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricToJson;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class MetricBatchJsonTelemetryBlockWriterTest {

  private MetricToJson metricToJson;
  private Gauge gauge;
  private Attributes commonAttributes;
  private MetricBatch metricBatch;

  @BeforeEach
  void setup() {
    commonAttributes = new Attributes().put("key", "val");
    gauge = new Gauge("gauge", 3d, 555, new Attributes());
    metricBatch = new MetricBatch(Collections.singletonList(gauge), commonAttributes);
    metricToJson = mock(MetricToJson.class);
  }

  @Test
  @DisplayName("Formatting with telemetry attributes is structured correctly")
  void testTelemetryJsonBlock() throws Exception {
    String expectedTelemetryJsonBlock =
        "\"metrics\":[{\"name\":\"gauge\",\"type\":\"gauge\",\"value\":3.0,\"timestamp\":555}]";
    when(metricToJson.writeGaugeJson(gauge))
        .thenReturn("{\"name\":\"gauge\",\"type\":\"gauge\",\"value\":3.0,\"timestamp\":555}");

    StringBuilder stringBuilder = new StringBuilder();
    MetricBatchJsonTelemetryBlockWriter testClass =
        new MetricBatchJsonTelemetryBlockWriter(metricToJson);
    testClass.appendTelemetryJson(metricBatch, stringBuilder);

    JSONAssert.assertEquals(expectedTelemetryJsonBlock, stringBuilder.toString(), false);
  }

  @Test
  void testInvalidMetrics() {
    Gauge invalidGauge1 = new Gauge("crumb", Float.NaN, System.currentTimeMillis(), new Attributes());
    Gauge invalidGauge2 = new Gauge("crumb", Float.NEGATIVE_INFINITY, System.currentTimeMillis(), new Attributes());
    List<Metric> metrics = Arrays.asList(gauge, invalidGauge1, invalidGauge2);
    metricBatch = new MetricBatch(metrics, commonAttributes);
    when(metricToJson.writeGaugeJson(gauge)).thenReturn("valid one");
    when(metricToJson.writeGaugeJson(invalidGauge1)).thenReturn("SUPER DUPER INVALID");
    when(metricToJson.writeGaugeJson(invalidGauge2)).thenReturn("SUPER DUPER INVALID");
    MetricBatchJsonTelemetryBlockWriter testClass =
            new MetricBatchJsonTelemetryBlockWriter(metricToJson);
    StringBuilder builder = new StringBuilder();
    testClass.appendTelemetryJson(metricBatch, builder);
    assertEquals("\"metrics\":[valid one]", builder.toString());
  }
}
