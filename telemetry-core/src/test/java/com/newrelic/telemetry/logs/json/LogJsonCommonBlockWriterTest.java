/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.logs.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.gson.stream.JsonWriter;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.logs.LogBatch;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogJsonCommonBlockWriterTest {

  private AttributesJson attributesJson;

  @BeforeEach
  void setup() {
    attributesJson = mock(AttributesJson.class);
  }

  @Test
  void testAppendJsonWithCommonAttributes() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

    String expected = "{\"common\":{\"attributes\":{\"You\":\"Wish\"}}}";
    LogBatch batch = new LogBatch(Collections.emptyList(), new Attributes().put("You", "Wish"));

    when(attributesJson.toJson(batch.getCommonAttributes().asMap()))
        .thenReturn("{\"You\":\"Wish\"}");
    LogJsonCommonBlockWriter testClass = new LogJsonCommonBlockWriter(attributesJson);
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
    LogBatch batch = new LogBatch(Collections.emptyList(), new Attributes());

    LogJsonCommonBlockWriter testClass = new LogJsonCommonBlockWriter(attributesJson);
    testClass.appendCommonJson(batch, jsonWriter);

    assertEquals(expected, out.toString());
  }
}
