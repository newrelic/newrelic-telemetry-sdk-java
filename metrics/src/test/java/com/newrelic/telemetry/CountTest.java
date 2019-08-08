/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    assertEquals(expectedValue, count.getValue() + 1);
    assertEquals(expectedStart, count.getStartTimeMs());
    assertEquals(expectedEnd, count.getEndTimeMs());
    assertEquals(expectedAttributes.asMap(), count.getAttributes());
  }
}
