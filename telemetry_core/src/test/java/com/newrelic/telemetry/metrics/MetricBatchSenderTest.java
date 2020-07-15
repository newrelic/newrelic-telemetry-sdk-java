/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics;

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
import com.newrelic.telemetry.metrics.json.MetricBatchMarshaller;
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
    URL url = URI.create("https://metric-api.newrelic.com/metric/v1").toURL();

    HttpPoster poster = mock(HttpPoster.class);
    Metric metric = new Gauge("foo", 12.1, 1234L, new Attributes().put("a", "b"));
    Collection<Metric> metrics = Collections.singletonList(metric);
    MetricBatch batch = new MetricBatch(metrics, new Attributes().put("f", "b"));
    Supplier<HttpPoster> posterSupplier = () -> poster;

    ArgumentCaptor<Map> headersCaptor = ArgumentCaptor.forClass(Map.class);
    when(poster.post(eq(url), headersCaptor.capture(), isA(byte[].class), anyString()))
        .thenReturn(httpResponse);

    MetricBatchSender metricBatchSender = MetricBatchSender.create(posterSupplier, baseConfig);

    Response result = metricBatchSender.sendBatch(batch);
    assertEquals(expected, result);
    assertTrue(((String) headersCaptor.getValue().get("User-Agent")).endsWith(" second"));
  }
}
