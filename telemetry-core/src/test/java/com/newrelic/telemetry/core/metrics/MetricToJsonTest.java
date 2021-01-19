/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.newrelic.telemetry.core.Attributes;
import com.newrelic.telemetry.core.metrics.json.MetricToJson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class MetricToJsonTest {

  MetricToJson metricToJson;

  @BeforeEach
  void setup() {
    metricToJson = new MetricToJson();
  }

  @Test
  void testSummaryMetricJson() throws Exception {

    Attributes attributes = new Attributes().put("key", "val");

    Summary summary = new Summary("summary", 3, 33d, 55d, 66d, 555, 666, attributes);
    String json = metricToJson.writeSummaryJson(summary);

    String expected =
        "{\"name\":\"summary\",\"type\":\"summary\",\"value\":{\"count\":3,\"sum\":33.0,\"min\":55.0,\"max\":66.0},\"timestamp\":555,\"interval.ms\":111,\"attributes\":{\"key\":\"val\"}}";
    JSONAssert.assertEquals(expected, json, false);
  }

  @Test
  public void testGauge() throws Exception {
    long now = System.currentTimeMillis();
    Gauge gauge = new Gauge("trev", 99.91, now, new Attributes().put("a", "b"));
    String json = metricToJson.writeGaugeJson(gauge);
    String expected =
        "{\"name\":\"trev\",\"type\":\"gauge\",\"value\":99.91,\"timestamp\":"
            + now
            + ",\"attributes\":{\"a\":\"b\"}}";
    assertEquals(expected, json);
  }

  @Test
  public void testCount() throws Exception {
    long now = System.currentTimeMillis();
    long later = now + 1250;
    Count count = new Count("saw", 387.1, now, later, new Attributes().put("b", "c"));
    String json = metricToJson.writeCountJson(count);
    String expected =
        "{\"name\":\"saw\",\"type\":\"count\",\"value\":387.1,\"timestamp\":"
            + now
            + ",\"interval.ms\":1250,\"attributes\":{\"b\":\"c\"}}";
    assertEquals(expected, json);
  }
}
