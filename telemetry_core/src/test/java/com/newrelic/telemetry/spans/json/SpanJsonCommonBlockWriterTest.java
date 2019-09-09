/*
 * ---------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.spans.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.json.JsonWriter;
import com.newrelic.telemetry.spans.SpanBatch;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SpanJsonCommonBlockWriterTest {

  private AttributesJson attributesJson;

  @BeforeEach
  void setup() {
    attributesJson = mock(AttributesJson.class);
  }

  @Test
  void testAppendJsonWithCommonAttributesAndTraceId() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);
    String expected =
        "{\"common\":{\"trace.id\":\"12\\\"3\",\"attributes\":{\"cleverKey\":\"cleverValue\"}}}";
    SpanBatch batch =
        new SpanBatch(
            Collections.emptyList(), new Attributes().put("cleverKey", "cleverValue"), "12\"3");

    when(attributesJson.toJson(batch.getCommonAttributes().asMap()))
        .thenReturn("{\"cleverKey\":\"cleverValue\"}");
    SpanJsonCommonBlockWriter testClass = new SpanJsonCommonBlockWriter(attributesJson);
    jsonWriter
        .beginObject(); // Because we are testing through a real writer, we have to give it object
    // context in order to do fragment work
    testClass.appendCommonJson(batch, jsonWriter);
    jsonWriter.endObject();

    assertEquals(expected, out.toString());
  }

  @Test
  void testAppendJsonWithTraceIdNoCommonAttributes() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

    String expected = "{\"common\":{\"trace.id\":\"123\"}}";
    SpanBatch batch = new SpanBatch(Collections.emptyList(), new Attributes(), "123");

    SpanJsonCommonBlockWriter testClass = new SpanJsonCommonBlockWriter(attributesJson);
    jsonWriter
        .beginObject(); // Because we are testing through a real writer, we have to give it object
    // context in order to do fragment work
    testClass.appendCommonJson(batch, jsonWriter);
    jsonWriter.endObject();
    assertEquals(expected, out.toString());
  }

  @Test
  void testAppendJsonWithCommonAttributesNoTraceId() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

    String expected = "{\"common\":{\"attributes\":{\"You\":\"Wish\"}}}";
    SpanBatch batch = new SpanBatch(Collections.emptyList(), new Attributes().put("You", "Wish"));

    when(attributesJson.toJson(batch.getCommonAttributes().asMap()))
        .thenReturn("{\"You\":\"Wish\"}");
    SpanJsonCommonBlockWriter testClass = new SpanJsonCommonBlockWriter(attributesJson);
    jsonWriter
        .beginObject(); // Because we are testing through a real writer, we have to give it object
    // context in order to do fragment work
    testClass.appendCommonJson(batch, jsonWriter);
    jsonWriter.endObject();

    assertEquals(expected, out.toString());
  }

  @Test
  void testAppendJsonNoTraceIdNoCommonAttributes() {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

    String expected = "";
    SpanBatch batch = new SpanBatch(Collections.emptyList(), new Attributes());

    SpanJsonCommonBlockWriter testClass = new SpanJsonCommonBlockWriter(attributesJson);
    testClass.appendCommonJson(batch, jsonWriter);

    assertEquals(expected, out.toString());
  }
}
