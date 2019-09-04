package com.newrelic.telemetry.spans.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import org.junit.jupiter.api.Test;

class SpanJsonTelemetryBlockWriterTest {

  @Test
  void testHappyPath() {
    StringBuilder sb = new StringBuilder();
    Span span1 = Span.builder("123").attributes(new Attributes().put("a", "b")).build();
    Span span2 = Span.builder("456").attributes(new Attributes().put("c", "d")).build();
    Collection<Span> telemetry = Arrays.asList(span1, span2);
    Attributes commonAttributes = new Attributes().put("come", "on");
    SpanBatch batch = new SpanBatch(telemetry, commonAttributes);
    String span1Expected = "{\"id\":\"123\",\"attributes\":{\"a\":\"b\"}}";
    String span2Expected = "{\"id\":\"456\",\"attributes\":{\"c\":\"d\"}}";
    String expected = "\"spans\":[" + span1Expected + "," + span2Expected + "]";

    AttributesJson attributesJson =
        new AttributesJson() {
          @Override
          public String toJson(Map<String, Object> attributes) {
            if (attributes.containsKey("a")) {
              return "\"attributes\":{\"a\":\"b\"}";
            }
            if (attributes.containsKey("c")) {
              return "\"attributes\":{\"c\":\"d\"}";
            }
            return "IDK";
          }
        };
    SpanJsonTelemetryBlockWriter testClass = new SpanJsonTelemetryBlockWriter(attributesJson);

    testClass.appendTelemetryJson(batch, sb);
    String result = sb.toString();

    assertEquals(expected, result);
  }

  @Test
  void testOtherImportantCases() {
    fail("build me");
  }
}
