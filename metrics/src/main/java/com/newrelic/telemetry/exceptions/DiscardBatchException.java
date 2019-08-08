/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.exceptions;

/** This exception is thrown when the request should be discarded and not retried. */
public class DiscardBatchException extends ResponseException {

  public DiscardBatchException() {
    super("The New Relic API failed to process this request and it should not be retried.");
  }
}
