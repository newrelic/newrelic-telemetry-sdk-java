/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.logs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.logs.json.LogBatchMarshaller;
import com.newrelic.telemetry.transport.BatchDataSender;
import java.util.Collections;
import org.junit.jupiter.api.Test;

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
    when(sender.send(json)).thenReturn(response);

    LogBatchSender testClass = new LogBatchSender(marshaller, sender);

    Response result = testClass.sendBatch(batch);
    assertEquals(response, result);
  }

  @Test
  void testEmptyBatch() throws Exception {
    LogBatchSender testClass = new LogBatchSender(null, null);
    LogBatch batch = new LogBatch(Collections.emptyList(), new Attributes());
    Response response = testClass.sendBatch(batch);
    assertEquals(202, response.getStatusCode());
  }
}
