/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core.logs.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.newrelic.telemetry.core.Attributes;
import com.newrelic.telemetry.core.json.AttributesJson;
import com.newrelic.telemetry.core.logs.Log;
import com.newrelic.telemetry.core.logs.LogBatch;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LogJsonTelemetryBlockWriterTest {

  @Test
  void testHappyPath() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

    Log log1 =
        Log.builder()
            .timestamp(99999)
            .serviceName("Hot.Service")
            .level("DEBUG")
            .message("log message 1")
            .attributes(new Attributes().put("a", "b"))
            .build();

    Log log2 =
        Log.builder()
            .timestamp(88888)
            .serviceName("Cold.\"Light\".Service")
            .level("INFO")
            .message("log message 2")
            .attributes(new Attributes().put("c", "d"))
            .build();

    Collection<Log> telemetry = Arrays.asList(log1, log2);
    Attributes commonAttributes = new Attributes().put("come", "on");
    LogBatch batch = new LogBatch(telemetry, commonAttributes);
    String log1Expected =
        "{\"timestamp\":99999,"
            + "\"attributes\":{\"a\":\"b\",\"service.name\":\"Hot.Service\",\"log.level\":\"DEBUG\"},\"message\":\"log message 1\"}";
    String log2Expected =
        "{\"timestamp\":88888,"
            + "\"attributes\":{\"c\":\"d\",\"service.name\":\"Cold.\\\"Light\\\".Service\",\"log.level\":\"INFO\"},\"message\":\"log message 2\"}";
    String expected = "{\"logs\":[" + log1Expected + "," + log2Expected + "]}";

    AttributesJson attributesJson = new AttributesJson();
    LogJsonTelemetryBlockWriter testClass = new LogJsonTelemetryBlockWriter(attributesJson);

    jsonWriter
        .beginObject(); // Because we are testing through a real writer, we have to give it object
    // context in order to do fragment work
    testClass.appendTelemetryJson(batch, jsonWriter);
    jsonWriter.endObject();
    String result = out.toString();

    assertEquals(expected, result);
  }

  @Test
  void testMinimum() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

    Log log = Log.builder().timestamp(12345).build();
    LogBatch logBatch = new LogBatch(Collections.singleton(log), new Attributes());

    LogJsonTelemetryBlockWriter testClass = new LogJsonTelemetryBlockWriter(new AttributesJson());

    jsonWriter
        .beginObject(); // Because we are testing through a real writer, we have to give it object
    // context in order to do fragment work
    testClass.appendTelemetryJson(logBatch, jsonWriter);
    jsonWriter.endObject();

    String result = out.toString();

    String expected = "{\"logs\":[{\"timestamp\":12345,\"attributes\":{}}]}";
    assertEquals(expected, result);
  }

  @Test
  void testThrowable() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

    Log log = Log.builder().timestamp(5555).throwable(new Exception("exception message")).build();
    LogBatch logBatch = new LogBatch(Collections.singleton(log), new Attributes());

    LogJsonTelemetryBlockWriter testClass = new LogJsonTelemetryBlockWriter(new AttributesJson());

    jsonWriter
        .beginObject(); // Because we are testing through a real writer, we have to give it object
    // context in order to do fragment work
    testClass.appendTelemetryJson(logBatch, jsonWriter);
    jsonWriter.endObject();

    String result = out.toString();

    Map<String, Object> resultData = new Gson().fromJson(result, Map.class);
    List<Map<String, Object>> logs = (List<Map<String, Object>>) resultData.get("logs");
    Map<String, Object> logEntry = logs.get(0);
    Map<String, Object> attributes = (Map<String, Object>) logEntry.get("attributes");
    assertNotNull(attributes);
    assertEquals("exception message", attributes.get("error.message"));
    assertEquals("java.lang.Exception", attributes.get("error.class"));
    assertNotNull(attributes.get("error.stack"));
  }

  /** This case should be guarded against at a higher level in the calling code. */
  @Test
  void testNoLogs() throws Exception {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

    LogBatch logBatch = new LogBatch(Collections.emptyList(), new Attributes());

    LogJsonTelemetryBlockWriter testClass = new LogJsonTelemetryBlockWriter(new AttributesJson());

    jsonWriter
        .beginObject(); // Because we are testing through a real writer, we have to give it object
    // context in order to do fragment work
    testClass.appendTelemetryJson(logBatch, jsonWriter);
    jsonWriter.endObject();

    String result = out.toString();

    String expected = "{\"logs\":[]}";
    assertEquals(expected, result);
  }

  @Test
  void testAttributesSetButNotProperties() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

    Attributes attrs =
        new Attributes()
            .put("service.name", "ipanema")
            .put("parent.id", "0xff")
            .put("duration.ms", 101)
            .put("name", "lucy");
    Log log = Log.builder().timestamp(12345).attributes(attrs).build();
    LogBatch logBatch = new LogBatch(Collections.singleton(log), new Attributes());

    LogJsonTelemetryBlockWriter testClass = new LogJsonTelemetryBlockWriter(new AttributesJson());

    jsonWriter
        .beginObject(); // Because we are testing through a real writer, we have to give it object
    // context in order to do fragment work
    testClass.appendTelemetryJson(logBatch, jsonWriter);
    jsonWriter.endObject();

    String result = out.toString();

    String expected =
        "{\"logs\":[{\"timestamp\":12345,\"attributes\":{"
            + "\"duration.ms\":101,"
            + "\"service.name\":\"ipanema\","
            + "\"name\":\"lucy\","
            + "\"parent.id\":\"0xff\""
            + "}}]}";
    assertEquals(expected, result);
  }

  @Test
  void testNullAttributesDontOverrideAndAreOmitted() throws IOException {
    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

    Attributes attrs =
        new Attributes()
            .put("service.name", (String) null)
            .put("message", (String) null)
            .put("log.level", (String) null)
            .put("error.message", (String) null)
            .put("error.class", (String) null)
            .put("error.stack", (String) null);
    Log log =
        Log.builder()
            .timestamp(12345)
            .attributes(attrs)
            .serviceName("my service")
            .message("message")
            .level("DEBUG")
            .throwable(new Exception("exception message"))
            .build();
    LogBatch logBatch = new LogBatch(Collections.singleton(log), new Attributes());

    LogJsonTelemetryBlockWriter testClass = new LogJsonTelemetryBlockWriter(new AttributesJson());

    jsonWriter
        .beginObject(); // Because we are testing through a real writer, we have to give it object
    // context in order to do fragment work
    testClass.appendTelemetryJson(logBatch, jsonWriter);
    jsonWriter.endObject();

    String result = out.toString();

    Map<String, Object> resultData = new Gson().fromJson(result, Map.class);
    List<Map<String, Object>> logs = (List<Map<String, Object>>) resultData.get("logs");
    Map<String, Object> logEntry = logs.get(0);
    assertEquals("message", logEntry.get("message"));
    Map<String, Object> attributes = (Map<String, Object>) logEntry.get("attributes");
    assertNotNull(attributes);
    assertEquals("DEBUG", attributes.get("log.level"));
    assertEquals("my service", attributes.get("service.name"));
    assertEquals("exception message", attributes.get("error.message"));
    assertEquals("java.lang.Exception", attributes.get("error.class"));
    assertNotNull(attributes.get("error.stack"));
  }
}
