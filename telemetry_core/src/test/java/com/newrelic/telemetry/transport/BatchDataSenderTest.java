/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.telemetry.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableMap;
import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.http.HttpResponse;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import org.junit.jupiter.api.Test;

class BatchDataSenderTest {

  @Test
  void testSend_noSecondaryUserAgent() throws Exception {
    URL endpointURl = new URL("http://foo.com");
    HttpPoster httpPoster = mock(HttpPoster.class);
    Map<String, String> headers =
        ImmutableMap.of(
            "User-Agent", "NewRelic-Java-TelemetrySDK/UnknownVersion",
            "Api-Key", "api-key",
            "Content-Encoding", "gzip");
    // note: not testing the gzipping here
    when(httpPoster.post(
            eq(endpointURl), eq(headers), any(), eq("application/json; charset=utf-8")))
        .thenReturn(new HttpResponse("yepyep", 202, "OK", Collections.emptyMap()));

    BatchDataSender testClass =
        new BatchDataSender(httpPoster, "api-key", endpointURl, false, null);

    Response response = testClass.send("{}");

    assertEquals(new Response(202, "OK", "yepyep"), response);
  }

  @Test
  void testSecondaryUserAgent() throws Exception {
    URL endpointURl = new URL("http://foo.com");
    HttpPoster httpPoster = mock(HttpPoster.class);
    Map<String, String> headers =
        ImmutableMap.of(
            "User-Agent", "NewRelic-Java-TelemetrySDK/UnknownVersion mySpecialUserAgent/1.0",
            "Api-Key", "api-key",
            "Content-Encoding", "gzip");
    // note: not testing the gzipping here
    when(httpPoster.post(
            eq(endpointURl), eq(headers), any(), eq("application/json; charset=utf-8")))
        .thenReturn(new HttpResponse("yepyep", 202, "OK", Collections.emptyMap()));

    BatchDataSender testClass =
        new BatchDataSender(httpPoster, "api-key", endpointURl, false, "mySpecialUserAgent/1.0");

    Response response = testClass.send("{}");

    assertEquals(new Response(202, "OK", "yepyep"), response);
  }
}
