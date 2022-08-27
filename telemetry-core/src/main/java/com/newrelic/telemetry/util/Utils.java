/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.util;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

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

  /**
   * Generate a random UUID using ThreadLocalRandom.
   *
   * @return a UUID conforming to V4 of RFC 4122
   */
  public static UUID generateUUID() {
    byte[] randomBytes = new byte[16];
    ThreadLocalRandom.current().nextBytes(randomBytes);

    // Conform to the behavior of UUID.randomUUID()
    // version 4, variant 2: https://www.ietf.org/rfc/rfc4122.txt
    randomBytes[6]  &= 0x0f;  /* clear version        */
    randomBytes[6]  |= 0x40;  /* set to version 4     */
    randomBytes[8]  &= 0x3f;  /* clear variant        */
    randomBytes[8]  |= 0x80;  /* set to IETF variant  */

    // UUID(byte[]) is private, so replicate the logic here to pack bytes into longs
    long msb = 0;
    long lsb = 0;
    for (int i=0; i<8; i++)
      msb = (msb << 8) | (randomBytes[i] & 0xff);
    for (int i=8; i<16; i++)
      lsb = (lsb << 8) | (randomBytes[i] & 0xff);

    return new UUID(msb, lsb);
  }
}
