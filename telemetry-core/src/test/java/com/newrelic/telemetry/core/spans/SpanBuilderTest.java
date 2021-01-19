/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core.spans;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpanBuilderTest {

  @Test
  void testSpanErrors() {
    assertFalse(Span.builder("123").build().isError());
    assertTrue(Span.builder("123").withError().build().isError());
  }
}
