package com.newrelic.telemetry.spans;

import static com.newrelic.telemetry.Telemetry.Type.SPAN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Telemetry.Type;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SpanBatchTest {
  @Test
  void testType() {
    SpanBatch testClass =
        new SpanBatch(
            Collections.emptyList(), new Attributes().put("superAwesomeKey", "megaAwesomeValue"));
    Type result = testClass.getType();
    assertEquals(SPAN, result);
  }

  @Test
  void testWithTraceId() {
    SpanBatch testClass =
        new SpanBatch(Collections.emptyList(), new Attributes().put("a", "b"), "magic");
    assertEquals("magic", testClass.getTraceId().get());
  }

  @Test
  void testWithoutTraceId() {
    SpanBatch testClass = new SpanBatch(Collections.emptyList(), new Attributes().put("a", "b"));
    assertTrue(testClass.getTraceId().isPresent());
  }
}
