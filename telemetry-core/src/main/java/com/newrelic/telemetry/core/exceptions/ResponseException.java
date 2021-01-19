/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core.exceptions;

import com.newrelic.telemetry.core.metrics.MetricBatch;
import com.newrelic.telemetry.core.metrics.MetricBatchSender;

/**
 * This class represents a response that doesn't have a 202 status code. There are 4 concrete
 * implementations of this Exception, each of which has a different recommended reaction by the
 * caller. Thrown by {@link MetricBatchSender#sendBatch(MetricBatch)}
 *
 * <ul>
 *   <li>{@link RetryWithSplitException} : Thrown when the submitted batch was too large to be
 *       processed by the New Relic ingest API. The recommended course of action is to split the
 *       batch and retry with the smaller batches.
 *   <li>{@link RetryWithRequestedWaitException} : Thrown when there is a recoverable problem
 *       submitting the batch to the ingest API. The exception contains the number of seconds that
 *       the caller should wait until retrying.
 *   <li>{@link RetryWithBackoffException} : Thrown when there is a transient, recoverable problem
 *       submitting the batch to the ingest API. The recommendation is to retry the request with
 *       exponential backoff.
 *   <li>{@link DiscardBatchException} : Thrown when a there is a non-recoverable problem with
 *       submitting the batch to the ingest API. The batch should be discarded.
 * </ul>
 */
public abstract class ResponseException extends Exception {

  ResponseException(String message) {
    super(message);
  }

  ResponseException(String message, Throwable cause) {
    super(message, cause);
  }
}
