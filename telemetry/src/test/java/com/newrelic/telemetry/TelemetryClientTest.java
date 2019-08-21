/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.exceptions.RetryWithBackoffException;
import com.newrelic.telemetry.exceptions.RetryWithRequestedWaitException;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
import com.newrelic.telemetry.model.Attributes;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

class TelemetryClientTest {

  private MetricBatch batch;

  @BeforeEach
  void setup() {
    Metric metric = makeMetric();
    batch = makeBatch(singleton(metric));
  }

  private MetricBatch makeBatch(Collection<Metric> metrics) {
    return new MetricBatch(metrics, new Attributes().put("foo", "bar"));
  }

  @Test
  void sendHappyPath() throws Exception {
    MetricBatchSender batchSender = mock(MetricBatchSender.class);
    CountDownLatch sendLatch = new CountDownLatch(1);
    when(batchSender.sendBatch(batch)).thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(batchSender);

    testClass.sendBatch(batch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
  }

  @Test
  void sendGeneratesRetryWithBackoff() throws Exception {
    MetricBatchSender batchSender = mock(MetricBatchSender.class);
    CountDownLatch sendLatch = new CountDownLatch(1);
    // First time explodes, second time succeeds
    when(batchSender.sendBatch(batch))
        .thenAnswer(
            invocation -> {
              throw new RetryWithBackoffException();
            })
        .thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(batchSender);

    testClass.sendBatch(batch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
  }

  @Test
  void sendGeneratesRetryWithRequestedBackoff() throws Exception {
    MetricBatchSender batchSender = mock(MetricBatchSender.class);
    CountDownLatch sendLatch = new CountDownLatch(1);
    when(batchSender.sendBatch(batch))
        .thenAnswer(
            invocation -> {
              throw new RetryWithRequestedWaitException(15, TimeUnit.MILLISECONDS);
            })
        .thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(batchSender);

    testClass.sendBatch(batch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
  }

  @Test
  void sendGeneratesRetryWithSplit() throws Exception {
    MetricBatchSender batchSender = mock(MetricBatchSender.class);
    MetricBatch batch = makeBatchOf3Metrics();
    // 1 for initial failure, then 1 for each part of the split
    CountDownLatch sendLatch = new CountDownLatch(3);
    when(batchSender.sendBatch(batch))
        .thenAnswer(
            invocation -> {
              sendLatch.countDown();
              throw new RetryWithSplitException();
            });
    List<MetricBatch> splitBatch = batch.split();

    when(batchSender.sendBatch(splitBatch.get(0))).thenAnswer(countDown(sendLatch));
    when(batchSender.sendBatch(splitBatch.get(1))).thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(batchSender);

    testClass.sendBatch(batch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
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

  private Metric makeMetric() {
    return new Count(
        UUID.randomUUID().toString(),
        99,
        System.currentTimeMillis() - 100,
        System.currentTimeMillis(),
        new Attributes().put("bar", "baz"));
  }
}
