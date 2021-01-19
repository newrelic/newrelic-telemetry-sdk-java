/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.telemetry.core.events.json;

import com.newrelic.telemetry.core.Attributes;
import com.newrelic.telemetry.core.events.Event;
import com.newrelic.telemetry.core.events.EventBatch;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class EventBatchMarshallerTest {

  private EventBatchMarshaller eventBatchMarshaller;

  @BeforeEach
  void setup() {
    eventBatchMarshaller = new EventBatchMarshaller();
  }

  @Test
  public void test_simple_serialize() throws Exception {
    Attributes commonAttributes = new Attributes();
    commonAttributes
        .put("escapeMe", "\"quoted\"")
        .put("double", 3.14d)
        .put("float", 4.32f)
        .put("int", 5)
        .put("long", 384949494949499999L)
        .put("boolean", true)
        .put("number", new BigDecimal("55.555"));

    EventBatch eb =
        new EventBatch(
            Collections.singletonList(new Event("testJIT", new Attributes(), 1586413929145L)),
            commonAttributes);

    String json = eventBatchMarshaller.toJson(eb);

    String expected =
        "[{\"timestamp\":1586413929145,\"eventType\":\"testJIT\",\"escapeMe\":\"\\\"quoted\\\"\",\"number\":55.555,\"boolean\":true,\"double\":3.14,\"float\":4.32,\"int\":5,\"long\":384949494949499999}]";
    JSONAssert.assertEquals(expected, json, true);
  }

  @Test
  public void mapToJson() throws Exception {
    long eventTimeMillis = 888777666222L;
    Event event =
        new Event(
            "jitThing",
            new Attributes()
                .put("foo", "bar")
                .put("bar", "baz")
                .put("quux", true)
                .put("zzz", 819)
                .put("yyy", 2.997),
            eventTimeMillis);
    String expected =
        "{\"eventType\":\"jitThing\", \"timestamp\":888777666222, \"foo\":\"bar\",\"bar\":\"baz\",\"quux\":true,\"zzz\":819,\"yyy\":2.997}";

    String json = EventBatchMarshaller.mapToJson(event);
    JSONAssert.assertEquals(expected, json, false);
  }
}
