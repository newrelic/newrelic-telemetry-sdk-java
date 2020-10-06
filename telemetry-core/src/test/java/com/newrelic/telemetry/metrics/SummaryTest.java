/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.newrelic.telemetry.Attributes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class SummaryTest {

  @Test
  @DisplayName("Summary data is stored and returned correctly")
  void testRawSummaryMetric() {
    String expectedName = "summaryMetricName";
    int expectedCount = 10;
    double expectedSum = 1000.00;
    double expectedMin = 123.45;
    double expectedMax = 543.21;
    long expectedStart = System.currentTimeMillis();
    long expectedEnd = System.currentTimeMillis() + 100;
    Attributes expectedAttributes =
        new Attributes().put("key1", "value1").put("key2", true).put("key3", 123.456);

    Summary summary =
        new Summary(
            expectedName,
            expectedCount,
            expectedSum,
            expectedMin,
            expectedMax,
            expectedStart,
            expectedEnd,
            expectedAttributes);

    assertEquals(expectedName, summary.getName());
    assertEquals(expectedCount, summary.getCount());
    assertEquals(expectedSum, summary.getSum());
    assertEquals(expectedMin, summary.getMin());
    assertEquals(expectedMax, summary.getMax());
    assertEquals(expectedStart, summary.getStartTimeMs());
    assertEquals(expectedEnd, summary.getEndTimeMs());
    assertEquals(expectedAttributes.asMap(), summary.getAttributes());
  }
}
