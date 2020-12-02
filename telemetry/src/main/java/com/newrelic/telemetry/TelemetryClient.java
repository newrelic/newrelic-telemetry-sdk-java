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
import java.util.concurrent.RejectedExecutionException;
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
  private static final int DEFAULT_SHUTDOWN_SECONDS = 3;
  private static final boolean DEFAULT_IS_DAEMON = true;
  private static final int DEFAULT_MAX_TELEMETRY_LIMIT = 1_000_000;

  private final EventBatchSender eventBatchSender;
  private final MetricBatchSender metricBatchSender;
  private final SpanBatchSender spanBatchSender;
  private final LimitingScheduler scheduler;
  private final int shutdownSeconds;
  private final LogBatchSender logBatchSender;
  private NotificationHandler notificationHandler = new LoggingNotificationHandler(LOG);

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
    this(
        metricBatchSender,
        spanBatchSender,
        eventBatchSender,
        logBatchSender,
        DEFAULT_SHUTDOWN_SECONDS,
        DEFAULT_IS_DAEMON);
  }

  /**
   * Create a new TelemetryClient instance, with four senders and seconds to wait for shutdown.
   *
   * @param metricBatchSender The sender for dimensional metrics.
   * @param spanBatchSender The sender for distributed tracing spans.
   * @param eventBatchSender The sender for custom events
   * @param logBatchSender The sender for log entries.
   * @param shutdownSeconds num of seconds to wait for graceful shutdown of its executor
   * @param useDaemonThread A flag to decide user-threads or daemon-threads
   */
  public TelemetryClient(
      MetricBatchSender metricBatchSender,
      SpanBatchSender spanBatchSender,
      EventBatchSender eventBatchSender,
      LogBatchSender logBatchSender,
      int shutdownSeconds,
      boolean useDaemonThread) {
    this(
        metricBatchSender,
        spanBatchSender,
        eventBatchSender,
        logBatchSender,
        shutdownSeconds,
        useDaemonThread,
        DEFAULT_MAX_TELEMETRY_LIMIT);
  }

  /**
   * Create a new TelemetryClient instance, with four senders and seconds to wait for shutdown. You
   * can also specify if the backing threads should be daemon threads, and the max number of
   * telemetry to buffer.
   *
   * @param metricBatchSender The sender for dimensional metrics.
   * @param spanBatchSender The sender for distributed tracing spans.
   * @param eventBatchSender The sender for custom events
   * @param logBatchSender The sender for log entries.
   * @param shutdownSeconds num of seconds to wait for graceful shutdown of its executor
   * @param useDaemonThread A flag to decide user-threads or daemon-threads
   * @param maxTelemetryBuffer The max number of telemetry to buffer
   */
  public TelemetryClient(
      MetricBatchSender metricBatchSender,
      SpanBatchSender spanBatchSender,
      EventBatchSender eventBatchSender,
      LogBatchSender logBatchSender,
      int shutdownSeconds,
      boolean useDaemonThread,
      int maxTelemetryBuffer) {
    this.metricBatchSender = metricBatchSender;
    this.spanBatchSender = spanBatchSender;
    this.eventBatchSender = eventBatchSender;
    this.logBatchSender = logBatchSender;
    this.shutdownSeconds = shutdownSeconds;
    this.scheduler = buildScheduler(useDaemonThread, maxTelemetryBuffer);
  }

  private interface BatchSender {
    void sendBatch(TelemetryBatch<?> batch) throws ResponseException;
  }

  /**
   * Send a batch of {@link com.newrelic.telemetry.metrics.Metric} instances, with standard retry
   * logic. This happens on a background thread, asynchronously, so currently there will be no
   * feedback to the caller outside of the logs.
   *
   * @param batch batch metrics to be applied
   */
  public void sendBatch(MetricBatch batch) {
    scheduleBatchSend(
        (b) -> metricBatchSender.sendBatch((MetricBatch) b), batch, 0, TimeUnit.SECONDS);
  }

  /**
   * Send a batch of {@link com.newrelic.telemetry.spans.Span} instances, with standard retry logic.
   * This happens on a background thread, asynchronously, so currently there will be no feedback to
   * the caller outside of the logs.
   *
   * @param batch to be sent
   */
  public void sendBatch(SpanBatch batch) {
    scheduleBatchSend((b) -> spanBatchSender.sendBatch((SpanBatch) b), batch, 0, TimeUnit.SECONDS);
  }

  /**
   * Send a batch of {@link com.newrelic.telemetry.events.Event} instances, with standard retry
   * logic. This happens on a background thread, asynchronously, so currently there will be no
   * feedback to the caller outside of the logs.
   *
   * @param batch to be sent
   */
  public void sendBatch(EventBatch batch) {
    scheduleBatchSend(
        (b) -> eventBatchSender.sendBatch((EventBatch) b), batch, 0, TimeUnit.SECONDS);
  }

  /**
   * Send a batch of {@link com.newrelic.telemetry.logs.Log} entries, with standard retry logic.
   * This happens on a background thread, asynchronously, so currently there will be no feedback to
   * the caller outside of the logs.
   *
   * @param batch to be sent
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

    if (scheduler.isTerminated()) {
      return;
    }
    try {
      scheduler.schedule(
          batch.size(), () -> sendWithErrorHandling(sender, batch, backoff), waitTime, timeUnit);
    } catch (RejectedExecutionException e) {
      if (notificationHandler != null) {
        notificationHandler.noticeError("Problem scheduling batch : ", e, batch);
      }
    }
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
      if (notificationHandler != null) {
        notificationHandler.noticeError(
            "Received a fatal exception from the New Relic API. Aborting metric batch send.",
            e,
            batch);
      }
    } catch (Exception e) {
      if (notificationHandler != null) {
        notificationHandler.noticeError("Unexpected failure when sending data.", e, batch);
      }
    }
  }

  private <T extends Telemetry> void splitAndSend(
      BatchSender sender, TelemetryBatch<T> batch, RetryWithSplitException e) {
    if (notificationHandler != null) {
      notificationHandler.noticeInfo(
          "Metric batch size too large, splitting and retrying.", e, batch);
    }
    List<TelemetryBatch<T>> splitBatches = batch.split();
    splitBatches.forEach(
        metricBatch -> scheduleBatchSend(sender, metricBatch, 0, TimeUnit.SECONDS));
  }

  private void retry(
      BatchSender sender,
      TelemetryBatch<? extends Telemetry> batch,
      RetryWithRequestedWaitException e) {
    if (notificationHandler != null) {
      notificationHandler.noticeInfo(
          String.format(
              "Metric batch sending failed. Retrying failed batch after %d %s",
              e.getWaitTime(), e.getTimeUnit().toString()),
          batch);
    }
    scheduleBatchSend(sender, batch, e.getWaitTime(), e.getTimeUnit());
  }

  private void backoff(
      BatchSender sender, TelemetryBatch<? extends Telemetry> batch, Backoff backoff) {

    long newWaitTime = backoff.nextWaitMs();
    if (newWaitTime == -1) {
      if (notificationHandler != null) {
        notificationHandler.noticeError(
            String.format(
                "Max retries exceeded.  Dropping %d pieces of telemetry data!", batch.size()),
            batch);
      }
      return;
    }
    if (notificationHandler != null) {
      notificationHandler.noticeInfo(
          String.format(
              "Metric batch sending failed. Backing off %d %s", newWaitTime, TimeUnit.MILLISECONDS),
          batch);
    }
    scheduleBatchSend(sender, batch, newWaitTime, TimeUnit.MILLISECONDS, backoff);
  }

  /** Cleanly shuts down the background Executor thread. */
  public void shutdown() {
    LOG.info("Shutting down the TelemetryClient background Executor");
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(shutdownSeconds, TimeUnit.SECONDS)) {
        LOG.warn("couldn't shutdown within timeout");
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      LOG.error("interrupted graceful shutdown", e);
      Thread.currentThread().interrupt();
    }
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
    return create(httpPosterCreator, new BaseConfig(insertApiKey));
  }

  /**
   * Create a fully operational {@link TelemetryClient} from a BaseConfig instance
   *
   * @param httpPosterCreator A {@link Supplier} used to create an {@link HttpPoster} instance.
   * @param baseConfig the base configuration
   * @return A fully operational TelemetryClient instance.
   */
  public static TelemetryClient create(
      Supplier<HttpPoster> httpPosterCreator, BaseConfig baseConfig) {
    MetricBatchSender metricBatchSender = MetricBatchSender.create(httpPosterCreator, baseConfig);
    SpanBatchSender spanBatchSender = SpanBatchSender.create(httpPosterCreator, baseConfig);
    EventBatchSender eventBatchSender = EventBatchSender.create(httpPosterCreator, baseConfig);
    LogBatchSender logBatchSender = LogBatchSender.create(httpPosterCreator, baseConfig);
    return new TelemetryClient(
        metricBatchSender, spanBatchSender, eventBatchSender, logBatchSender);
  }

  /**
   * Create ScheduledExecutorService from a parameter given by constructor
   *
   * @param useDaemonThread A flag to decide user-threads or daemon-threads
   * @param maxTelemetryBuffer Max number of telemetry to buffer
   * @return ScheduledExecutorService
   */
  private static LimitingScheduler buildScheduler(boolean useDaemonThread, int maxTelemetryBuffer) {
    ScheduledExecutorService executor =
        Executors.newSingleThreadScheduledExecutor(
            r -> {
              Thread thread = new Thread(r);
              thread.setDaemon(useDaemonThread);
              return thread;
            });
    return new LimitingScheduler(executor, maxTelemetryBuffer);
  }

  /**
   * Provide a {@link NotificationHandler} to the {@link TelemetryClient} for handling {@link
   * ResponseException}
   *
   * @param notificationHandler The {@link NotificationHandler} to use
   */
  public void withNotificationHandler(NotificationHandler notificationHandler) {
    this.notificationHandler = notificationHandler;
  }
}
