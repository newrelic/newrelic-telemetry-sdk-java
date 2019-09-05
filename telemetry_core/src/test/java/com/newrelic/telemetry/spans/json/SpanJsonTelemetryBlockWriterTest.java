/*
 * ---------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.spans.json;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.Test;

class SpanJsonTelemetryBlockWriterTest {

  @Test
  void testHappyPath() {
    StringBuilder sb = new StringBuilder();
    Span span1 =
        Span.builder("123")
            .traceId("987")
            .timestamp(99999)
            .serviceName("Hot.Service")
            .durationMs(100.0)
            .name("Trevor")
            .parentId("Jonathan")
            .attributes(new Attributes().put("a", "b"))
            .build();

    Span span2 =
        Span.builder("456")
            .traceId("654")
            .timestamp(88888)
            .serviceName("Cold.Service")
            .durationMs(200.0)
            .name("Joleene")
            .parentId("Agatha")
            .attributes(new Attributes().put("c", "d"))
            .build();

    Collection<Span> telemetry = Arrays.asList(span1, span2);
    Attributes commonAttributes = new Attributes().put("come", "on");
    SpanBatch batch = new SpanBatch(telemetry, commonAttributes);
    String span1Expected =
        "{\"id\":\"123\","
            + "\"trace.id\":\"987\","
            + "\"timestamp\":99999,"
            + "\"attributes\":{\"a\":\"b\"}}";
    String span2Expected =
        "{\"id\":\"456\","
            + "\"trace.id\":\"654\","
            + "\"timestamp\":88888,"
            + "\"attributes\":{\"c\":\"d\"}}";
    String expected = "\"spans\":[" + span1Expected + "," + span2Expected + "]";

    AttributesJson attributesJson =
        attributes -> {
          if (attributes.containsKey("a")) {
            assertEquals("Hot.Service", attributes.get("service.name"));
            assertEquals(100.0, attributes.get("duration.ms"));
            assertEquals("Trevor", attributes.get("name"));
            assertEquals("Jonathan", attributes.get("parent.id"));
            return "{\"a\":\"b\"}";
          }
          if (attributes.containsKey("c")) {
            assertEquals("Cold.Service", attributes.get("service.name"));
            assertEquals(200.0, attributes.get("duration.ms"));
            assertEquals("Joleene", attributes.get("name"));
            assertEquals("Agatha", attributes.get("parent.id"));
            return "{\"c\":\"d\"}";
          }
          return "IDK";
        };
    SpanJsonTelemetryBlockWriter testClass = new SpanJsonTelemetryBlockWriter(attributesJson);

    testClass.appendTelemetryJson(batch, sb);
    String result = sb.toString();

    assertEquals(expected, result);
  }
}
