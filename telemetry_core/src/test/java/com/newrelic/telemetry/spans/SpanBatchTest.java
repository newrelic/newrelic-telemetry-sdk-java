/*
 * ---------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
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
