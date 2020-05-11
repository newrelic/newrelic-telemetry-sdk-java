/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.events.Event;
import com.newrelic.telemetry.events.EventBatch;
import com.newrelic.telemetry.events.EventBatchSender;
import com.newrelic.telemetry.exceptions.RetryWithBackoffException;
import com.newrelic.telemetry.exceptions.RetryWithRequestedWaitException;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.Metric;
import com.newrelic.telemetry.metrics.MetricBatch;
import com.newrelic.telemetry.metrics.MetricBatchSender;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

class TelemetryClientTest {

  private MetricBatch metricBatch;
  private SpanBatch spanBatch;
  private EventBatch eventBatch;

  @BeforeEach
  void setup() {
    metricBatch = makeBatch(singleton(makeMetric()));
    spanBatch = new SpanBatch(singleton(makeSpan()), new Attributes().put("foo", "bar"));
    eventBatch = new EventBatch(singleton(makeEvent()));
  }

  @Test
  void sendMetricsHappyPath() throws Exception {
    MetricBatchSender batchSender = mock(MetricBatchSender.class);
    CountDownLatch sendLatch = new CountDownLatch(1);
    when(batchSender.sendBatch(metricBatch)).thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(batchSender, null, null, logBatchSender);

    testClass.sendBatch(metricBatch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
  }

  @Test
  void sendSpansHappyPath() throws Exception {
    SpanBatchSender batchSender = mock(SpanBatchSender.class);
    CountDownLatch sendLatch = new CountDownLatch(1);
    when(batchSender.sendBatch(spanBatch)).thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(null, batchSender, null, logBatchSender);

    testClass.sendBatch(spanBatch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
  }

  @Test
  void sendEventsHappyPath() throws Exception {
    EventBatchSender batchSender = mock(EventBatchSender.class);
    CountDownLatch sendLatch = new CountDownLatch(1);
    when(batchSender.sendBatch(eventBatch)).thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(null, null, batchSender, logBatchSender);

    testClass.sendBatch(eventBatch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
  }

  @Test
  void sendGeneratesRetryWithBackoff() throws Exception {
    MetricBatchSender batchSender = mock(MetricBatchSender.class);
    CountDownLatch sendLatch = new CountDownLatch(1);
    // First time explodes, second time succeeds
    Answer<Object> requestRetry =
        invocation -> {
          throw new RetryWithBackoffException();
        };
    when(batchSender.sendBatch(metricBatch))
        .thenAnswer(requestRetry)
        .thenAnswer(requestRetry)
        .thenAnswer(requestRetry)
        .thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(batchSender, null, null, logBatchSender);

    testClass.sendBatch(metricBatch);
    boolean result = sendLatch.await(10, TimeUnit.SECONDS);
    assertTrue(result);
  }

  @Test
  void sendGeneratesRetryWithRequestedBackoff() throws Exception {
    MetricBatchSender batchSender = mock(MetricBatchSender.class);
    CountDownLatch sendLatch = new CountDownLatch(1);
    when(batchSender.sendBatch(metricBatch))
        .thenAnswer(
            invocation -> {
              throw new RetryWithRequestedWaitException(15, TimeUnit.MILLISECONDS);
            })
        .thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(batchSender, null, null, logBatchSender);

    testClass.sendBatch(metricBatch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
  }

  @Test
  void sendGeneratesRetryWithSplit() throws Exception {
    MetricBatchSender batchSender = mock(MetricBatchSender.class);
    MetricBatch batch = makeBatchOf3Metrics();
    // 1 for initial failure, then 1 for each part of the split
    CountDownLatch sendLatch = new CountDownLatch(3);
    AtomicBoolean batch1Seen = new AtomicBoolean(false);
    AtomicBoolean batch2Seen = new AtomicBoolean(false);

    when(batchSender.sendBatch(isA(MetricBatch.class)))
        .thenAnswer(
            invocation -> {
              MetricBatch batchParam = invocation.getArgument(0);
              if (batchParam.size() == 3) {
                sendLatch.countDown();
                throw new RetryWithSplitException();
              }
              if (batchParam.size() == 1) { // first part of split batch
                batch1Seen.set(true);
              }
              if (batchParam.size() == 2) { // second part of split batch
                batch2Seen.set(true);
              }
              sendLatch.countDown();
              return null;
            });

    TelemetryClient testClass = new TelemetryClient(batchSender, null, null, logBatchSender);

    testClass.sendBatch(batch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
    assertTrue(batch1Seen.get());
    assertTrue(batch2Seen.get());
  }

  private Answer<Object> countDown(CountDownLatch latch) {
    return invocation -> {
      latch.countDown();
      return null;
    };
  }

  private MetricBatch makeBatchOf3Metrics() {
    Metric metric1 = makeMetric();
    Metric metric2 = makeMetric();
    Metric metric3 = makeMetric();
    List<Metric> metrics = Arrays.asList(metric1, metric2, metric3);
    return makeBatch(metrics);
  }

  private static Metric makeMetric() {
    return new Count(
        UUID.randomUUID().toString(),
        99,
        System.currentTimeMillis() - 100,
        System.currentTimeMillis(),
        new Attributes().put("bar", "baz"));
  }

  private static Span makeSpan() {
    return Span.builder("spanId").timestamp(6666).traceId("traceId").build();
  }

  private static Event makeEvent() {
    return new Event("JITStuff", new Attributes().put("class", "my.Foo"));
  }

  private MetricBatch makeBatch(Collection<Metric> metrics) {
    return new MetricBatch(metrics, new Attributes().put("foo", "bar"));
  }
}
