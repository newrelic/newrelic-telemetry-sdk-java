/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.newrelic.telemetry.json.AttributesJson;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class AttributesJsonTest {

  @Test
  void testEmpty() throws Exception {
    AttributesJson attributesJson = new AttributesJson();
    assertEquals("{}", attributesJson.toJson(Collections.emptyMap()));
  }

  @Test
  void testSimpleCase() throws Exception {
    AttributesJson attributesJson = new AttributesJson();
    JSONAssert.assertEquals(
        "{\"foo\":\"bar\"}", attributesJson.toJson(Collections.singletonMap("foo", "bar")), false);
  }
}
