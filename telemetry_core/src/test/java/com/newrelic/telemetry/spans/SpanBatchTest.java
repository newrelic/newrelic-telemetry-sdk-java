package com.newrelic.telemetry.spans;

import static com.newrelic.telemetry.Telemetry.Type.SPAN;
import static org.junit.jupiter.api.Assertions.*;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Telemetry.Type;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SpanBatchTest {
  @Test
  void testType() {
    SpanBatch testClass = new SpanBatch(Collections.emptyList(), new Attributes().put("superAwesomeKey", "megaAwesomeValue"), "456");
    Type result = testClass.getType();
    assertEquals(SPAN, result);
  }

  @Test
  void testNullTraceId() {
    assertThrows(NullPointerException.class, () -> {SpanBatch testClass = new SpanBatch(Collections.emptyList(), new Attributes().put("superAwesomeKey", "megaAwesomeValue"), null);});
  }
}