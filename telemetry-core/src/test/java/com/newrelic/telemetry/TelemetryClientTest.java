/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.events.Event;
import com.newrelic.telemetry.events.EventBatch;
import com.newrelic.telemetry.events.EventBatchSender;
import com.newrelic.telemetry.exceptions.RetryWithBackoffException;
import com.newrelic.telemetry.exceptions.RetryWithRequestedWaitException;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.http.HttpResponse;
import com.newrelic.telemetry.logs.Log;
import com.newrelic.telemetry.logs.LogBatch;
import com.newrelic.telemetry.logs.LogBatchSender;
import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.Metric;
import com.newrelic.telemetry.metrics.MetricBatch;
import com.newrelic.telemetry.metrics.MetricBatchSender;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
import com.newrelic.telemetry.util.Utils;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;

class TelemetryClientTest {

  private MetricBatch metricBatch;
  private SpanBatch spanBatch;
  private EventBatch eventBatch;
  private LogBatch logBatch;
  private MetricBatchSender batchSender;

  @BeforeEach
  void setup() {
    metricBatch = makeBatch(singleton(makeMetric()));
    spanBatch = new SpanBatch(singleton(makeSpan()), new Attributes().put("foo", "bar"));
    eventBatch = new EventBatch(singleton(makeEvent()));
    logBatch = new LogBatch(singleton(makeLog()), new Attributes().put("foo", "bar"));
    batchSender = mock(MetricBatchSender.class);
  }

  @Test
  void sendMetricsHappyPath() throws Exception {
    CountDownLatch sendLatch = new CountDownLatch(1);
    when(batchSender.sendBatch(metricBatch)).thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(batchSender, null, null, null);

    testClass.sendBatch(metricBatch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
  }

  @Test
  void sendSpansHappyPath() throws Exception {
    SpanBatchSender batchSender = mock(SpanBatchSender.class);
    CountDownLatch sendLatch = new CountDownLatch(1);
    when(batchSender.sendBatch(spanBatch)).thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(null, batchSender, null, null);

    testClass.sendBatch(spanBatch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
  }

  @Test
  void sendEventsHappyPath() throws Exception {
    EventBatchSender batchSender = mock(EventBatchSender.class);
    CountDownLatch sendLatch = new CountDownLatch(1);
    when(batchSender.sendBatch(eventBatch)).thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(null, null, batchSender, null);

    testClass.sendBatch(eventBatch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
  }

  @Test
  void sendLogsHappyPath() throws Exception {
    LogBatchSender batchSender = mock(LogBatchSender.class);
    CountDownLatch sendLatch = new CountDownLatch(1);
    when(batchSender.sendBatch(logBatch)).thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(null, null, null, batchSender);

    testClass.sendBatch(logBatch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
  }

  @Test
  void sendGeneratesRetryWithBackoff() throws Exception {
    CountDownLatch sendLatch = new CountDownLatch(1);
    when(batchSender.sendBatch(metricBatch))
        .thenThrow(new RetryWithBackoffException())
        .thenThrow(new RetryWithBackoffException())
        .thenThrow(new RetryWithBackoffException())
        .thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(batchSender, null, null, null);

    testClass.sendBatch(metricBatch);
    boolean result = sendLatch.await(10, TimeUnit.SECONDS);
    assertTrue(result);
  }

  @Test
  void sendGeneratesRetryWithRequestedBackoff() throws Exception {
    CountDownLatch sendLatch = new CountDownLatch(1);
    when(batchSender.sendBatch(metricBatch))
        .thenThrow(new RetryWithRequestedWaitException(15, TimeUnit.MILLISECONDS))
        .thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(batchSender, null, null, null);

    testClass.sendBatch(metricBatch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
  }

  @Test
  void sendGeneratesRetryWithRequestedBackoffWithCustomNotificationHandler() throws Exception {
    CountDownLatch sendLatch = new CountDownLatch(1);
    when(batchSender.sendBatch(metricBatch))
        .thenThrow(new RetryWithRequestedWaitException(15, TimeUnit.MILLISECONDS))
        .thenAnswer(countDown(sendLatch));

    TelemetryClient testClass = new TelemetryClient(batchSender, null, null, null);
    CustomNotificationHandler customNotificationHandler = new CustomNotificationHandler();
    testClass.withNotificationHandler(customNotificationHandler);
    testClass.sendBatch(metricBatch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
    assertEquals(Collections.emptyList(), customNotificationHandler.errorMessages);
    assertEquals(
        Collections.singletonList(
            "Batch sending failed. Retrying failed batch after 15 MILLISECONDS"),
        customNotificationHandler.infoMessages);
  }

  @Test
  void sendGeneratesRetryWithSplit() throws Exception {
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

    TelemetryClient testClass = new TelemetryClient(batchSender, null, null, null);
    CustomNotificationHandler customNotificationHandler = new CustomNotificationHandler();
    testClass.withNotificationHandler(customNotificationHandler);
    testClass.sendBatch(batch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
    assertTrue(batch1Seen.get());
    assertTrue(batch2Seen.get());

    testClass.sendBatch(metricBatch);
    assertTrue(result);
    assertEquals(Collections.emptyList(), customNotificationHandler.errorMessages);
    assertEquals(
        Collections.singletonList("Batch size too large, splitting and retrying."),
        customNotificationHandler.infoMessages);
  }

  @Test
  void retryWithSplitGoesOverLimit() throws Exception {
    // The first schedule will work, we'll be right at capacity which is fine...but the handling of
    // the SplitWithRetryException still takes place in the job, so the telemetry for that initial
    // batch is still resident.  Therefore, the split will push us over and the subsequent schedules
    // will fail, and send will not get called again.
    int maxTelemetry = 3;
    MetricBatch batch = makeBatchOf3Metrics();
    // 1 for initial failure, then 1 for each part of the split
    CountDownLatch sendLatch = new CountDownLatch(1);
    AtomicBoolean batch1Seen = new AtomicBoolean(false);
    AtomicBoolean batch2Seen = new AtomicBoolean(false);
    AtomicInteger sendCount = new AtomicInteger();

    when(batchSender.sendBatch(isA(MetricBatch.class)))
        .thenAnswer(
            invocation -> {
              sendCount.incrementAndGet();
              MetricBatch batchParam = invocation.getArgument(0);
              if (batchParam.size() == 3) {
                sendLatch.countDown();
                throw new RetryWithSplitException();
              }
              return null;
            });

    TelemetryClient testClass =
        new TelemetryClient(batchSender, null, null, null, 1, true, maxTelemetry);
    CustomNotificationHandler customNotificationHandler = new CustomNotificationHandler();
    testClass.withNotificationHandler(customNotificationHandler);
    testClass.sendBatch(batch);
    testClass.shutdown();
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    assertTrue(result);
    assertEquals(1, sendCount.get());
    assertEquals(0, customNotificationHandler.errorMessages.size());
    assertEquals(Collections.emptyList(), customNotificationHandler.errorMessages);
    assertEquals(
        Collections.singletonList("Batch size too large, splitting and retrying."),
        customNotificationHandler.infoMessages);
  }

  @Test
  void retryWithSplitDoesntGoOverLimit() throws Exception {
    int maxTelemetry = 6; // two times the largest failing batch size of 3
    MetricBatch batch = makeBatchOf3Metrics();
    // 1 for initial failure, then 1 for each part of the split
    CountDownLatch sendLatch = new CountDownLatch(3);
    AtomicBoolean batch1Seen = new AtomicBoolean(false);
    AtomicBoolean batch2Seen = new AtomicBoolean(false);
    AtomicInteger totalSent = new AtomicInteger(0);

    when(batchSender.sendBatch(isA(MetricBatch.class)))
        .thenAnswer(
            invocation -> {
              MetricBatch batchParam = invocation.getArgument(0);
              totalSent.addAndGet(batchParam.size());
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

    TelemetryClient testClass =
        new TelemetryClient(batchSender, null, null, null, 1, true, maxTelemetry);
    CustomNotificationHandler customNotificationHandler = new CustomNotificationHandler();
    testClass.withNotificationHandler(customNotificationHandler);
    testClass.sendBatch(batch);
    boolean result = sendLatch.await(3, TimeUnit.SECONDS);
    testClass.shutdown();
    assertEquals(6, totalSent.get());
    assertTrue(result);
    assertTrue(batch1Seen.get());
    assertTrue(batch2Seen.get());
    assertEquals(Collections.emptyList(), customNotificationHandler.errorMessages);
    assertEquals(
        Collections.singletonList("Batch size too large, splitting and retrying."),
        customNotificationHandler.infoMessages);
  }

  @Test
  void canCreateWithoutSecondaryUserAgent() throws Exception {
    BaseConfig baseConfig = new BaseConfig("123abc");
    HttpPoster poster = mock(HttpPoster.class);
    Supplier<HttpPoster> posterSupplier = () -> poster;
    Response expected = new Response(202, "okey", "bb");
    HttpResponse httpResponse =
        new HttpResponse(
            expected.getBody(),
            expected.getStatusCode(),
            expected.getStatusMessage(),
            new HashMap<>());
    CountDownLatch latch = new CountDownLatch(1);
    Event event = new Event("flim", new Attributes().put("x", "y"));
    EventBatch metrics = new EventBatch(singletonList(event), new Attributes().put("a", "b"));
    URL url = URI.create("https://insights-collector.newrelic.com/v1/accounts/events").toURL();

    TelemetryClient client = TelemetryClient.create(posterSupplier, baseConfig);

    ArgumentCaptor<Map> headersCaptor = ArgumentCaptor.forClass(Map.class);
    when(poster.post(eq(url), headersCaptor.capture(), isA(byte[].class), anyString()))
        .thenAnswer(
            (Answer<HttpResponse>)
                invocation -> {
                  latch.countDown();
                  return httpResponse;
                });

    client.sendBatch(metrics);
    assertTrue(latch.await(2, TimeUnit.SECONDS));
    String sentUserAgent = (String) headersCaptor.getValue().get("User-Agent");
    assertTrue(sentUserAgent.contains("TelemetrySDK"));
  }

  @Test
  void configureBackoff_usesCustomBackoffRetryCount() throws Exception {
    // Test with 2 retries: sendBatch always throws, backoff stops after 2 retries
    Backoff customBackoff2 = mock(Backoff.class);
    when(customBackoff2.nextWaitMs()).thenReturn(1L, 1L, -1L);
    // Always throw RetryWithBackoffException
    when(batchSender.sendBatch(metricBatch)).thenThrow(new RetryWithBackoffException());

    TelemetryClient testClass2 = new TelemetryClient(batchSender, null, null, null);
    testClass2.configureBackoff(customBackoff2);
    testClass2.sendBatch(metricBatch);
    Thread.sleep(200); // Wait for async retries
    // Should call nextWaitMs() exactly 3 times (2 retries, then stop)
    org.mockito.Mockito.verify(customBackoff2, org.mockito.Mockito.times(3)).nextWaitMs();
    // Should call sendBatch 3 times (initial + 2 retries)
    org.mockito.Mockito.verify(batchSender, org.mockito.Mockito.times(3)).sendBatch(metricBatch);

    // Reset mocks for next test
    org.mockito.Mockito.reset(batchSender);
    // Test with 3 retries: sendBatch always throws, backoff stops after 3 retries
    Backoff customBackoff3 = mock(Backoff.class);
    when(customBackoff3.nextWaitMs()).thenReturn(1L, 1L, 1L, -1L);
    when(batchSender.sendBatch(metricBatch)).thenThrow(new RetryWithBackoffException());

    TelemetryClient testClass3 = new TelemetryClient(batchSender, null, null, null);
    testClass3.configureBackoff(customBackoff3);
    testClass3.sendBatch(metricBatch);
    Thread.sleep(300); // Wait for async retries
    // Should call nextWaitMs() exactly 4 times (3 retries, then stop)
    org.mockito.Mockito.verify(customBackoff3, org.mockito.Mockito.times(4)).nextWaitMs();
    // Should call sendBatch 4 times (initial + 3 retries)
    org.mockito.Mockito.verify(batchSender, org.mockito.Mockito.times(4)).sendBatch(metricBatch);
  }

  @Test
  void testBufferFlushConfiguration() throws Exception {
    MetricBatchSender batchSender = mock(MetricBatchSender.class);
    TelemetryClient testClass =
        new TelemetryClient(batchSender, null, null, null, 3, true, 100, 80, 100);

    // Verify buffer usage tracking works
    assertEquals(0, testClass.getBufferUsagePercent());
    assertEquals(100, testClass.getAvailableBufferCapacity());

    testClass.shutdown();
  }

  @Test
  void testConfigureBufferFlushingAtRuntime() throws Exception {
    MetricBatchSender batchSender = mock(MetricBatchSender.class);
    TelemetryClient testClass = new TelemetryClient(batchSender, null, null, null);

    // Initially no flushing configured
    assertEquals(0, testClass.getBufferUsagePercent());

    // Configure flushing at runtime
    testClass.configureBufferFlushing(75, 100);

    // Should still work normally
    assertEquals(0, testClass.getBufferUsagePercent());

    testClass.shutdown();
  }

  @Test
  void testManualBufferFlush() throws Exception {
    MetricBatchSender batchSender = mock(MetricBatchSender.class);
    TelemetryClient testClass = new TelemetryClient(batchSender, null, null, null);

    // Manual flush should not throw exception
    testClass.flushBuffers();

    testClass.shutdown();
  }

  @Test
  void testFullConstructorWithFlushSettings() throws Exception {
    MetricBatchSender metricSender = mock(MetricBatchSender.class);
    SpanBatchSender spanSender = mock(SpanBatchSender.class);
    EventBatchSender eventSender = mock(EventBatchSender.class);
    LogBatchSender logSender = mock(LogBatchSender.class);

    TelemetryClient testClass =
        new TelemetryClient(
            metricSender, spanSender, eventSender, logSender, 3, true, 1000, 70, 200);

    // Verify the client was created successfully
    assertEquals(0, testClass.getBufferUsagePercent());
    assertEquals(1000, testClass.getAvailableBufferCapacity());

    testClass.shutdown();
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
        Utils.generateUUID().toString(),
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

  private Log makeLog() {
    return Log.builder().message("starting").build();
  }

  private MetricBatch makeBatch(Collection<Metric> metrics) {
    return new MetricBatch(metrics, new Attributes().put("foo", "bar"));
  }

  private static class CustomNotificationHandler implements NotificationHandler {
    List infoMessages = new ArrayList();
    List errorMessages = new ArrayList();

    @Override
    public void noticeInfo(
        String message, Exception exception, TelemetryBatch<? extends Telemetry> batch) {
      infoMessages.add(message);
    }

    @Override
    public void noticeError(
        String message, Throwable t, TelemetryBatch<? extends Telemetry> batch) {
      errorMessages.add(message);
    }
  }
}
