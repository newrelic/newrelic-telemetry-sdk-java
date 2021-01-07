/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.logs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.BaseConfig;
import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.http.HttpResponse;
import com.newrelic.telemetry.logs.json.LogBatchMarshaller;
import com.newrelic.telemetry.transport.BatchDataSender;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class LogBatchSenderTest {

  @Test
  void testSimpleSend() throws Exception {
    Log log = Log.builder().build();
    LogBatch batch = new LogBatch(Collections.singletonList(log), new Attributes().put("j", "k"));
    String json = "{a great document}";
    Response response = new Response(123, "OK", "yup");

    LogBatchMarshaller marshaller = mock(LogBatchMarshaller.class);
    BatchDataSender sender = mock(BatchDataSender.class);

    when(marshaller.toJson(batch)).thenReturn(json);
    when(sender.send(json, batch)).thenReturn(response);

    LogBatchSender testClass = new LogBatchSender(marshaller, sender);

    Response result = testClass.sendBatch(batch);
    assertEquals(response, result);
  }

  @Test
  public void sendBatchViaCreate() throws Exception {
    BaseConfig baseConfig = new BaseConfig("hi", true, "second");
    Response expected = new Response(202, "okey", "bb");
    HttpResponse httpResponse =
        new HttpResponse(
            expected.getBody(),
            expected.getStatusCode(),
            expected.getStatusMessage(),
            new HashMap<>());
    URL url = URI.create("https://log-api.newrelic.com/log/v1").toURL();

    HttpPoster poster = mock(HttpPoster.class);
    Log log = Log.builder().message("my log").build();
    Collection<Log> logs = Collections.singletonList(log);
    LogBatch batch = new LogBatch(logs, new Attributes().put("f", "b"));
    Supplier<HttpPoster> posterSupplier = () -> poster;

    ArgumentCaptor<Map> headersCaptor = ArgumentCaptor.forClass(Map.class);
    when(poster.post(eq(url), headersCaptor.capture(), isA(byte[].class), anyString()))
        .thenReturn(httpResponse);

    LogBatchSender logBatchSender = LogBatchSender.create(posterSupplier, baseConfig);

    Response result = logBatchSender.sendBatch(batch);
    assertEquals(expected, result);
    assertTrue(((String) headersCaptor.getValue().get("User-Agent")).endsWith(" second"));
  }

  @Test
  void testEmptyBatch() throws Exception {
    LogBatchSender testClass = new LogBatchSender(null, null);
    LogBatch batch = new LogBatch(Collections.emptyList(), new Attributes());
    Response response = testClass.sendBatch(batch);
    assertEquals(202, response.getStatusCode());
  }
}
