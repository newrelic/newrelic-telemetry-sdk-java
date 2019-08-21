/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.util;

/** A class with helper functions */
public class Utils {

  public static <E> E verifyNonNull(E input, String message) throws IllegalArgumentException {
    if (input == null) {
      throw new IllegalArgumentException(message);
    }
    return input;
  }

  public static <E> E verifyNonNull(E input) throws IllegalArgumentException {
    return verifyNonNull(input, "input cannot be null");
  }
}
