/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.exceptions.RetryWithBackoffException;
import com.newrelic.telemetry.exceptions.RetryWithRequestedWaitException;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
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

  private final MetricBatchSender sender;
  private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

  public TelemetryClient(MetricBatchSender sender) {
    this.sender = sender;
  }

  /**
   * Send a batch, with standard retry logic. This happens on a background thread, asynchronously,
   * so currently there will be no feedback to the caller outside of the logs.
   */
  public void sendBatch(MetricBatch batch) {
    scheduleBatchSend(batch, 0, TimeUnit.SECONDS);
  }

  private void scheduleBatchSend(MetricBatch batch, int waitTime, TimeUnit timeUnit) {
    executor.schedule(() -> sendWithErrorHandling(batch, waitTime, timeUnit), waitTime, timeUnit);
  }

  private void sendWithErrorHandling(MetricBatch batch, int preWaitTime, TimeUnit timeUnit) {
    try {
      sender.sendBatch(batch);
      LOG.debug("Metric batch sent");
    } catch (RetryWithBackoffException e) {
      backoff(batch, preWaitTime, timeUnit);
    } catch (RetryWithRequestedWaitException e) {
      retry(batch, e);
    } catch (RetryWithSplitException e) {
      splitAndSend(batch, timeUnit, e);
    } catch (ResponseException e) {
      LOG.error(
          "Received a fatal exception from the New Relic API. Aborting metric batch send.", e);
    } catch (Exception e) {
      LOG.error("Unexpected failure when sending data.", e);
    }
  }

  private void splitAndSend(MetricBatch batch, TimeUnit timeUnit, RetryWithSplitException e) {
    LOG.info("Metric batch size too large, splitting and retrying.", e);
    List<MetricBatch> splitBatches = batch.split();
    for (MetricBatch splitBatch : splitBatches) {
      scheduleBatchSend(splitBatch, 0, timeUnit);
    }
  }

  private void retry(MetricBatch batch, RetryWithRequestedWaitException e) {
    LOG.info(
        "Metric batch sending failed. Retrying failed batch after {} {}",
        e.getWaitTime(),
        e.getTimeUnit());
    scheduleBatchSend(batch, e.getWaitTime(), e.getTimeUnit());
  }

  private void backoff(MetricBatch batch, int preWaitTime, TimeUnit timeUnit) {
    int newWaitTime;
    if (preWaitTime == 0) {
      newWaitTime = 1;
      timeUnit = TimeUnit.SECONDS;
    } else {
      newWaitTime = preWaitTime * 2;
    }
    LOG.info("Metric batch sending failed. Backing off {} {}", newWaitTime, timeUnit);
    scheduleBatchSend(batch, newWaitTime, timeUnit);
  }

  /** Cleanly shuts down the background Executor thread. */
  public void shutdown() {
    executor.shutdown();
  }
}
