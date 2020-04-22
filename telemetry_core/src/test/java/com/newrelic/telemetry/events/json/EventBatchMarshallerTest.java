/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.telemetry.events.json;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.events.Event;
import com.newrelic.telemetry.events.EventBatch;
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
}
