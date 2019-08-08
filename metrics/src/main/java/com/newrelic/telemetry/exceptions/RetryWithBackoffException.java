/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.exceptions;

/**
 * This exception is thrown when the batch submission failed and the sender should follow a backoff
 * algorithm when retrying the submission.
 */
public class RetryWithBackoffException extends ResponseException {

  public RetryWithBackoffException() {
    super("The New Relic API suggests backing off exponentially on this request.");
  }
}
