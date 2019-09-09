package com.newrelic.telemetry.spans;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpanBuilderTest {

  @Test
  void testSpanErrors() {
    assertFalse(Span.builder("123").build().isError());
    assertTrue(Span.builder("123").isError().build().isError());
  }
}
