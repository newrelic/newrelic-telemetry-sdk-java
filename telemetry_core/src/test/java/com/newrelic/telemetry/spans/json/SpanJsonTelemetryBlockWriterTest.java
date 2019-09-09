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
import com.newrelic.telemetry.json.JsonWriter;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SpanJsonTelemetryBlockWriterTest {

  @Test
  void testHappyPath() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

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
            + "\"attributes\":{\"duration.ms\":100.0,\"a\":\"b\",\"service.name\":\"Hot.Service\",\"name\":\"Trevor\",\"parent.id\":\"Jonathan\"}}";
    String span2Expected =
        "{\"id\":\"456\","
            + "\"trace.id\":\"654\","
            + "\"timestamp\":88888,"
            + "\"attributes\":{\"duration.ms\":200.0,\"c\":\"d\",\"service.name\":\"Cold.Service\",\"name\":\"Joleene\",\"parent.id\":\"Agatha\"}}";
    String expected = "{\"spans\":[" + span1Expected + "," + span2Expected + "]}";

    AttributesJson attributesJson = new AttributesJson();
    SpanJsonTelemetryBlockWriter testClass = new SpanJsonTelemetryBlockWriter(attributesJson);

    jsonWriter.beginObject(); // Because we are testing through a real writer, we have to give it object context in order to do fragment work
    testClass.appendTelemetryJson(batch, jsonWriter);
    jsonWriter.endObject();
    String result = out.toString();

    assertEquals(expected, result);
  }

  @Test
  void testNoTraceId() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

    Span span = Span.builder("123").timestamp(12345).build();
    SpanBatch spanBatch = new SpanBatch(Collections.singleton(span), new Attributes());

    SpanJsonTelemetryBlockWriter testClass = new SpanJsonTelemetryBlockWriter(new AttributesJson());

    jsonWriter
        .beginObject(); // Because we are testing through a real writer, we have to give it object context in order to do fragment work
    testClass.appendTelemetryJson(spanBatch, jsonWriter);
    jsonWriter.endObject();

    String result = out.toString();

    String expected = "{\"spans\":[{\"id\":\"123\",\"timestamp\":12345,\"attributes\":{}}]}";
    assertEquals(expected, result);
  }

  @Test
  void testError() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

    Span span = Span.builder("667").timestamp(90210).withError().build();
    SpanBatch spanBatch = new SpanBatch(Collections.singleton(span), new Attributes());

    SpanJsonTelemetryBlockWriter testClass = new SpanJsonTelemetryBlockWriter(new AttributesJson());

    jsonWriter
        .beginObject(); // Because we are testing through a real writer, we have to give it object context in order to do fragment work
    testClass.appendTelemetryJson(spanBatch, jsonWriter);
    jsonWriter.endObject();

    String result = out.toString();

    String expected = "{\"spans\":[{\"id\":\"667\",\"timestamp\":90210,\"attributes\":{\"error\":true}}]}";
    assertEquals(expected, result);
  }

  /**
   * This case should be guarded against at a higher level in the calling code.
   */
  @Test
  void testNoSpans() throws Exception {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

    SpanBatch spanBatch = new SpanBatch(Collections.emptyList(), new Attributes());

    SpanJsonTelemetryBlockWriter testClass = new SpanJsonTelemetryBlockWriter(new AttributesJson());

    jsonWriter
        .beginObject(); // Because we are testing through a real writer, we have to give it object context in order to do fragment work
    testClass.appendTelemetryJson(spanBatch, jsonWriter);
    jsonWriter.endObject();

    String result = out.toString();

    String expected = "{\"spans\":[]}";
    assertEquals(expected, result);  }
}
