/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.exceptions.RetryWithBackoffException;
import com.newrelic.telemetry.exceptions.RetryWithRequestedWaitException;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
import com.newrelic.telemetry.metrics.MetricBatch;
import com.newrelic.telemetry.metrics.MetricBatchSender;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class should be the go-to spot for sending telemetry to New Relic. It includes the canonical
 * implementation of retry-logic that we recommend being used when interacting with the ingest APIs.
 *
 * <p>Note: This class creates a single threaded scheduled executor on which all sending happens. Be
 * sure to call {@link #shutdown()} if you don't want this background thread to keep the VM from
 * exiting.
 */
public class TelemetryClient {

  private static final Logger LOG = LoggerFactory.getLogger(TelemetryClient.class);

  private final MetricBatchSender metricBatchSender;
  private final SpanBatchSender spanBatchSender;
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  /**
   * Create a new TelemetryClient instance, with two senders. Note that if you don't intend to send
   * one of the telemetry types, you can pass in a null value for that sender.
   *
   * @param metricBatchSender The sender for dimensional metrics.
   * @param spanBatchSender The sender for distributed tracing spans.
   */
  public TelemetryClient(MetricBatchSender metricBatchSender, SpanBatchSender spanBatchSender) {
    this.metricBatchSender = metricBatchSender;
    this.spanBatchSender = spanBatchSender;
  }

  private interface BatchSender {
    void sendBatch(TelemetryBatch<?> batch) throws ResponseException;
  }

  /**
   * Send a batch of metrics, with standard retry logic. This happens on a background thread,
   * asynchronously, so currently there will be no feedback to the caller outside of the logs.
   */
  public void sendBatch(MetricBatch batch) {
    scheduleBatchSend(
        (b) -> metricBatchSender.sendBatch((MetricBatch) b), batch, 0, TimeUnit.SECONDS);
  }

  /**
   * Send a batch of spans, with standard retry logic. This happens on a background thread,
   * asynchronously, so currently there will be no feedback to the caller outside of the logs.
   */
  public void sendBatch(SpanBatch batch) {
    scheduleBatchSend((b) -> spanBatchSender.sendBatch((SpanBatch) b), batch, 0, TimeUnit.SECONDS);
  }

  private void scheduleBatchSend(
      BatchSender sender,
      TelemetryBatch<? extends Telemetry> batch,
      int waitTime,
      TimeUnit timeUnit) {
    executor.schedule(
        () -> sendWithErrorHandling(sender, batch, waitTime, timeUnit), waitTime, timeUnit);
  }

  private void sendWithErrorHandling(
      BatchSender batchSender,
      TelemetryBatch<? extends Telemetry> batch,
      int preWaitTime,
      TimeUnit timeUnit) {
    try {
      batchSender.sendBatch(batch);
      LOG.debug("Telemetry batch sent");
    } catch (RetryWithBackoffException e) {
      backoff(batchSender, batch, preWaitTime, timeUnit);
    } catch (RetryWithRequestedWaitException e) {
      retry(batchSender, batch, e);
    } catch (RetryWithSplitException e) {
      splitAndSend(batchSender, batch, timeUnit, e);
    } catch (ResponseException e) {
      LOG.error(
          "Received a fatal exception from the New Relic API. Aborting metric batch send.", e);
    } catch (Exception e) {
      LOG.error("Unexpected failure when sending data.", e);
    }
  }

  private <T extends Telemetry> void splitAndSend(
      BatchSender sender, TelemetryBatch<T> batch, TimeUnit timeUnit, RetryWithSplitException e) {
    LOG.info("Metric batch size too large, splitting and retrying.", e);
    List<TelemetryBatch<T>> splitBatches = batch.split();
    splitBatches.forEach(metricBatch -> scheduleBatchSend(sender, metricBatch, 0, timeUnit));
  }

  private void retry(
      BatchSender sender,
      TelemetryBatch<? extends Telemetry> batch,
      RetryWithRequestedWaitException e) {
    LOG.info(
        "Metric batch sending failed. Retrying failed batch after {} {}",
        e.getWaitTime(),
        e.getTimeUnit());
    scheduleBatchSend(sender, batch, e.getWaitTime(), e.getTimeUnit());
  }

  private void backoff(
      BatchSender sender,
      TelemetryBatch<? extends Telemetry> batch,
      int preWaitTime,
      TimeUnit timeUnit) {
    int newWaitTime;
    if (preWaitTime == 0) {
      newWaitTime = 1;
      timeUnit = TimeUnit.SECONDS;
    } else {
      newWaitTime = preWaitTime * 2;
    }
    LOG.info("Metric batch sending failed. Backing off {} {}", newWaitTime, timeUnit);
    scheduleBatchSend(sender, batch, newWaitTime, timeUnit);
  }

  /** Cleanly shuts down the background Executor thread. */
  public void shutdown() {
    executor.shutdown();
  }
}
