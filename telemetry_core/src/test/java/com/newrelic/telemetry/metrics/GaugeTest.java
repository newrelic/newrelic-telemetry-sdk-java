/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.newrelic.telemetry.Attributes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GaugeTest {

  @Test
  @DisplayName("Gauge data is stored and returned correctly")
  void testRawGaugeMetric() {
    String expectedName = "gaugeMetricName";
    double expectedValue = 123.45;
    long expectedTimestamp = System.currentTimeMillis();
    Attributes expectedAttributes =
        new Attributes().put("key1", "value1").put("key2", true).put("key3", 123.456);

    Gauge gauge = new Gauge(expectedName, expectedValue, expectedTimestamp, expectedAttributes);

    assertEquals(expectedName, gauge.getName());
    assertEquals(expectedValue, gauge.getValue());
    assertEquals(expectedTimestamp, gauge.getTimestamp());
    assertEquals(expectedAttributes.asMap(), gauge.getAttributes());
  }
}
