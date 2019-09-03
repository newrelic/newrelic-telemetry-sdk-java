package com.newrelic.telemetry.spans.json;

import static org.junit.jupiter.api.Assertions.*;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.Telemetry.Type;
import com.newrelic.telemetry.TelemetryBatch;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SpanJsonCommonBlockWriterTest {
  @Test
  void testAppendJson() {
    SpanJsonCommonBlockWriter testClass = new SpanJsonCommonBlockWriter();
    StringBuilder builder = new StringBuilder();
    SpanBatch batch = new SpanBatch(Collections.emptyList(), new Attributes().put("cleverKey", "cleverValue"), "123");
    testClass.appendCommonJson(batch, builder);
    String result = builder.toString();
    String expected = "\"common\": {\"traceId\": \"123\", \"attributes\": {\"cleverKey\": \"cleverValue\"}}";
    assertEquals(expected, result);
  }

  @Test
  void testAppendJsonNoCommonAttributes() {

  }
}