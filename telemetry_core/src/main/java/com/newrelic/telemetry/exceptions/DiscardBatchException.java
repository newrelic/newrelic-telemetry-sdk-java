/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.exceptions;

/** This exception is thrown when the request should be discarded and not retried. */
public class DiscardBatchException extends ResponseException {

  public DiscardBatchException() {
    super("The New Relic API failed to process this request and it should not be retried.");
  }
}
