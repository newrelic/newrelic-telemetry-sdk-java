/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.spans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.newrelic.telemetry.Attributes;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SpanBatchTest {

  @Test
  void testWithTraceId() {
    SpanBatch testClass =
        new SpanBatch(Collections.emptyList(), new Attributes().put("a", "b"), "magic");
    assertEquals("magic", testClass.getTraceId().get());
  }

  @Test
  void testWithoutTraceId() {
    SpanBatch testClass = new SpanBatch(Collections.emptyList(), new Attributes().put("a", "b"));
    assertFalse(testClass.getTraceId().isPresent());
  }
}
