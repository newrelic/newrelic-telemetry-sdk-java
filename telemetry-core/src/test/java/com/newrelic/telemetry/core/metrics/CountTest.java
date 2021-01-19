/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.newrelic.telemetry.core.Attributes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CountTest {

  @Test
  @DisplayName("Count data is stored and returned correctly")
  void testRawCountMetric() {
    String expectedName = "countMetricName";
    double expectedValue = 123.45;
    long expectedStart = System.currentTimeMillis();
    long expectedEnd = System.currentTimeMillis() + 100;
    Attributes expectedAttributes =
        new Attributes().put("key1", "value1").put("key2", true).put("key3", 123.456);

    Count count =
        new Count(expectedName, expectedValue, expectedStart, expectedEnd, expectedAttributes);

    assertEquals(expectedName, count.getName());
    assertEquals(expectedValue, count.getValue());
    assertEquals(expectedStart, count.getStartTimeMs());
    assertEquals(expectedEnd, count.getEndTimeMs());
    assertEquals(expectedAttributes.asMap(), count.getAttributes());
  }
}
