/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.metrics.json.MetricBatchMarshaller;
import com.newrelic.telemetry.transport.BatchDataSender;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class MetricBatchSenderTest {

  @Test
  void testSimpleSend() throws Exception {
    Metric metric = new Count("a", 12.1, 123, 456, new Attributes());
    MetricBatch batch =
        new MetricBatch(Collections.singletonList(metric), new Attributes().put("j", "k"));
    String json = "{a great document}";
    Response response = new Response(123, "OK", "yup");

    MetricBatchMarshaller marshaller = mock(MetricBatchMarshaller.class);
    BatchDataSender sender = mock(BatchDataSender.class);

    when(marshaller.toJson(batch)).thenReturn(json);
    when(sender.send(json)).thenReturn(response);

    MetricBatchSender testClass = new MetricBatchSender(marshaller, sender);

    Response result = testClass.sendBatch(batch);
    assertEquals(response, result);
  }

  @Test
  void testEmptyBatch() throws Exception {
    MetricBatchSender testClass = new MetricBatchSender(null, null);
    MetricBatch batch = new MetricBatch(Collections.emptyList(), new Attributes());
    Response response = testClass.sendBatch(batch);
    assertEquals(202, response.getStatusCode());
  }
}
