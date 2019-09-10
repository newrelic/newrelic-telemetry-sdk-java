/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.exceptions;

import com.newrelic.telemetry.metrics.MetricBatch;

/**
 * This exception is thrown when the request was too large and should be split in half and tried
 * again.
 *
 * @see MetricBatch#split()
 */
public class RetryWithSplitException extends ResponseException {

  public RetryWithSplitException() {
    super("Batch was too large. Please try splitting and resending smaller sized batches.");
  }
}
