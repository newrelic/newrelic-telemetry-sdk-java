/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core.util;

/** A class with helper functions */
public class Utils {

  public static <E> E verifyNonNull(E input, String message) throws IllegalArgumentException {
    if (input == null) {
      throw new IllegalArgumentException(message);
    }
    return input;
  }

  public static String verifyNonBlank(String input, String message)
      throws IllegalArgumentException {
    if (input == null || input.isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return input;
  }

  public static <E> E verifyNonNull(E input) throws IllegalArgumentException {
    return verifyNonNull(input, "input cannot be null");
  }
}
