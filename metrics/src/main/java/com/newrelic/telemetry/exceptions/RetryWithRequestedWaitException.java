/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.exceptions;

import java.util.concurrent.TimeUnit;

/** This exception is thrown when the request should be tried again after a period of time. */
public class RetryWithRequestedWaitException extends ResponseException {

  private final int waitTime;
  private final TimeUnit timeUnit;

  public RetryWithRequestedWaitException(int waitTime, TimeUnit timeUnit) {
    super("Please retry after " + waitTime + " " + timeUnit);
    this.waitTime = waitTime;
    this.timeUnit = timeUnit;
  }

  public int getWaitTime() {
    return waitTime;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }
}
