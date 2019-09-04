package com.newrelic.telemetry.spans.json;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.spans.SpanBatch;
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
  void testAppendJsonWithCommonAttributesAndTraceId() {
    String expected =
        "\"common\":{\"traceId\":\"123\",\"attributes\":{\"cleverKey\":\"cleverValue\"}}";
    StringBuilder builder = new StringBuilder();
    SpanBatch batch =
        new SpanBatch(
            Collections.emptyList(), new Attributes().put("cleverKey", "cleverValue"), "123");

    when(attributesJson.toJson(batch.getCommonAttributes().asMap()))
        .thenReturn("{\"cleverKey\":\"cleverValue\"}");
    SpanJsonCommonBlockWriter testClass = new SpanJsonCommonBlockWriter(attributesJson);
    testClass.appendCommonJson(batch, builder);

    assertEquals(expected, builder.toString());
  }

  @Test
  void testAppendJsonWithTraceIdNoCommonAttributes() {
    String expected = "\"common\":{\"traceId\":\"123\"}";
    StringBuilder builder = new StringBuilder();
    SpanBatch batch = new SpanBatch(Collections.emptyList(), new Attributes(), "123");

    SpanJsonCommonBlockWriter testClass = new SpanJsonCommonBlockWriter(attributesJson);
    testClass.appendCommonJson(batch, builder);

    assertEquals(expected, builder.toString());
  }

  @Test
  void testAppendJsonWithCommonAttributesNoTraceId() {
    String expected = "\"common\":{\"attributes\":{\"You\":\"Wish\"}}";
    StringBuilder builder = new StringBuilder();
    SpanBatch batch = new SpanBatch(Collections.emptyList(), new Attributes().put("You", "Wish"));

    when(attributesJson.toJson(batch.getCommonAttributes().asMap()))
        .thenReturn("{\"You\":\"Wish\"}");
    SpanJsonCommonBlockWriter testClass = new SpanJsonCommonBlockWriter(attributesJson);
    testClass.appendCommonJson(batch, builder);

    assertEquals(expected, builder.toString());
  }

  @Test
  void testAppendJsonNoTraceIdNoCommonAttributes() {
    String expected = "";
    StringBuilder builder = new StringBuilder();
    SpanBatch batch = new SpanBatch(Collections.emptyList(), new Attributes());

    SpanJsonCommonBlockWriter testClass = new SpanJsonCommonBlockWriter(attributesJson);
    testClass.appendCommonJson(batch, builder);

    assertEquals(expected, builder.toString());
  }
}
