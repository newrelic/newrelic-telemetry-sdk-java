/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import com.newrelic.telemetry.events.EventBatch;
import com.newrelic.telemetry.events.EventBatchSender;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.exceptions.RetryWithBackoffException;
import com.newrelic.telemetry.exceptions.RetryWithRequestedWaitException;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.logs.LogBatch;
import com.newrelic.telemetry.logs.LogBatchSender;
import com.newrelic.telemetry.metrics.MetricBatch;
import com.newrelic.telemetry.metrics.MetricBatchSender;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
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

  private final EventBatchSender eventBatchSender;
  private final MetricBatchSender metricBatchSender;
  private final SpanBatchSender spanBatchSender;
  private final LogBatchSender logBatchSender;

  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  /**
   * Create a new TelemetryClient instance, with four senders. Note that if you don't intend to send
   * one of the telemetry types, you can pass in a null value for that sender.
   *
   * @param metricBatchSender The sender for dimensional metrics.
   * @param spanBatchSender The sender for distributed tracing spans.
   * @param eventBatchSender The sender for custom events.
   * @param logBatchSender The sender for log entries.
   */
  public TelemetryClient(
      MetricBatchSender metricBatchSender,
      SpanBatchSender spanBatchSender,
      EventBatchSender eventBatchSender,
      LogBatchSender logBatchSender) {
    this.metricBatchSender = metricBatchSender;
    this.spanBatchSender = spanBatchSender;
    this.eventBatchSender = eventBatchSender;
    this.logBatchSender = logBatchSender;
  }

  /**
   * Create a new TelemetryClient instance, with two senders. Note that if you don't intend to send
   * one of the telemetry types, you can pass in a null value for that sender.
   *
   * @deprecated Use the constructor with all three senders, passing in null to the ones you don't
   *     care about.
   * @param metricBatchSender The sender for dimensional metrics.
   * @param spanBatchSender The sender for distributed tracing spans.
   */
  @Deprecated
  public TelemetryClient(MetricBatchSender metricBatchSender, SpanBatchSender spanBatchSender) {
    this(metricBatchSender, spanBatchSender, null, null);
  }

  private interface BatchSender {
    void sendBatch(TelemetryBatch<?> batch) throws ResponseException;
  }

  /**
   * Send a batch of {@link com.newrelic.telemetry.metrics.Metric} instances, with standard retry
   * logic. This happens on a background thread, asynchronously, so currently there will be no
   * feedback to the caller outside of the logs.
   */
  public void sendBatch(MetricBatch batch) {
    scheduleBatchSend(
        (b) -> metricBatchSender.sendBatch((MetricBatch) b), batch, 0, TimeUnit.SECONDS);
  }

  /**
   * Send a batch of {@link com.newrelic.telemetry.spans.Span} instances, with standard retry logic.
   * This happens on a background thread, asynchronously, so currently there will be no feedback to
   * the caller outside of the logs.
   */
  public void sendBatch(SpanBatch batch) {
    scheduleBatchSend((b) -> spanBatchSender.sendBatch((SpanBatch) b), batch, 0, TimeUnit.SECONDS);
  }

  /**
   * Send a batch of {@link com.newrelic.telemetry.events.Event} instances, with standard retry
   * logic. This happens on a background thread, asynchronously, so currently there will be no
   * feedback to the caller outside of the logs.
   */
  public void sendBatch(EventBatch batch) {
    scheduleBatchSend(
        (b) -> eventBatchSender.sendBatch((EventBatch) b), batch, 0, TimeUnit.SECONDS);
  }

  /**
   * Send a batch of {@link com.newrelic.telemetry.logs.Log} entries, with standard retry logic.
   * This happens on a background thread, asynchronously, so currently there will be no feedback to
   * the caller outside of the logs.
   */
  public void sendBatch(LogBatch batch) {
    scheduleBatchSend((b) -> logBatchSender.sendBatch((LogBatch) b), batch, 0, TimeUnit.SECONDS);
  }

  private void scheduleBatchSend(
      BatchSender sender,
      TelemetryBatch<? extends Telemetry> batch,
      long waitTime,
      TimeUnit timeUnit) {
    scheduleBatchSend(sender, batch, waitTime, timeUnit, Backoff.defaultBackoff());
  }

  private void scheduleBatchSend(
      BatchSender sender,
      TelemetryBatch<? extends Telemetry> batch,
      long waitTime,
      TimeUnit timeUnit,
      Backoff backoff) {
    executor.schedule(() -> sendWithErrorHandling(sender, batch, backoff), waitTime, timeUnit);
  }

  private void sendWithErrorHandling(
      BatchSender batchSender, TelemetryBatch<? extends Telemetry> batch, Backoff backoff) {
    try {
      batchSender.sendBatch(batch);
      LOG.debug("Telemetry batch sent");
    } catch (RetryWithBackoffException e) {
      backoff(batchSender, batch, backoff);
    } catch (RetryWithRequestedWaitException e) {
      retry(batchSender, batch, e);
    } catch (RetryWithSplitException e) {
      splitAndSend(batchSender, batch, e);
    } catch (ResponseException e) {
      LOG.error(
          "Received a fatal exception from the New Relic API. Aborting metric batch send.", e);
    } catch (Exception e) {
      LOG.error("Unexpected failure when sending data.", e);
    }
  }

  private <T extends Telemetry> void splitAndSend(
      BatchSender sender, TelemetryBatch<T> batch, RetryWithSplitException e) {
    LOG.info("Metric batch size too large, splitting and retrying.", e);
    List<TelemetryBatch<T>> splitBatches = batch.split();
    splitBatches.forEach(
        metricBatch -> scheduleBatchSend(sender, metricBatch, 0, TimeUnit.SECONDS));
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
      BatchSender sender, TelemetryBatch<? extends Telemetry> batch, Backoff backoff) {

    long newWaitTime = backoff.nextWaitMs();
    if (newWaitTime == -1) {
      LOG.error("Max retries exceeded.  Dropping {} pieces of telemetry data!", batch.size());
      return;
    }
    LOG.info("Metric batch sending failed. Backing off {} {}", newWaitTime, TimeUnit.MILLISECONDS);
    scheduleBatchSend(sender, batch, newWaitTime, TimeUnit.MILLISECONDS, backoff);
  }

  /** Cleanly shuts down the background Executor thread. */
  public void shutdown() {
    LOG.info("Shutting down the TelemetryClient background Executor");
    executor.shutdown();
  }

  /**
   * Create a fully operational {@link TelemetryClient} with all default options.
   *
   * @param httpPosterCreator A {@link Supplier} used to create an {@link HttpPoster} instance.
   * @param insertApiKey The New Relic Insert API to use.
   * @return A fully operational TelemetryClient instance.
   */
  public static TelemetryClient create(
      Supplier<HttpPoster> httpPosterCreator, String insertApiKey) {
    MetricBatchSender metricBatchSender =
        MetricBatchSender.create(
            MetricBatchSenderFactory.fromHttpImplementation(httpPosterCreator)
                .configureWith(insertApiKey)
                .build());

    SpanBatchSender spanBatchSender =
        SpanBatchSender.create(
            SpanBatchSenderFactory.fromHttpImplementation(httpPosterCreator)
                .configureWith(insertApiKey)
                .build());

    EventBatchSender eventBatchSender =
        EventBatchSender.create(
            EventBatchSenderFactory.fromHttpImplementation(httpPosterCreator)
                .configureWith(insertApiKey)
                .build());

    LogBatchSender logBatchSender =
        LogBatchSender.create(
            LogBatchSenderFactory.fromHttpImplementation(httpPosterCreator)
                .configureWith(insertApiKey)
                .build());
    return new TelemetryClient(
        metricBatchSender, spanBatchSender, eventBatchSender, logBatchSender);
  }
}
