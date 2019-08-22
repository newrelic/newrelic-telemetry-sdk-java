/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class MetricGsonGeneratorTest {

  Gson gson;
  MetricGsonGenerator metricGsonGenerator;

  @BeforeEach
  void setup() {
    gson = new GsonBuilder().create();
    metricGsonGenerator = new MetricGsonGenerator(gson);
  }

  @Test
  void testSummaryMetricJson() throws Exception {

    Attributes attributes = new Attributes().put("key", "val");

    Summary summary = new Summary("summary", 3, 33d, 55d, 66d, 555, 666, attributes);
    String json = metricGsonGenerator.writeSummaryJson(summary);

    String expected =
        "[{\"common\":{\"attributes\":{\"key\":\"val\"}},\"metrics\":[{\"name\":\"summary\",\"type\":\"summary\",\"value\":{\"count\":3,\"sum\":33.0,\"min\":55.0,\"max\":66.0},\"timestamp\":555,\"interval.ms\":111,\"attributes\":{}}]}]";
    JSONAssert.assertEquals(expected, json, false);
  }

  @Test
  public void testGauge() throws Exception {
    fail("build me");
  }

  @Test
  public void testCount() throws Exception {
    fail("build me");
  }
}
