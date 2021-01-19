/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.newrelic.telemetry.core.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ResponseTest {

  @Test
  @DisplayName("Response data is stored and returned correctly")
  void testResponse() {
    int expectedStatusCode = 202;
    String expectedStatusMessage = "statusMessage";
    String expectedBody = "body";

    Response response = new Response(expectedStatusCode, expectedStatusMessage, expectedBody);

    assertEquals(expectedStatusCode, response.getStatusCode());
    assertEquals(expectedStatusMessage, response.getStatusMessage());
    assertEquals(expectedBody, response.getBody());
  }
}
