/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.newrelic.telemetry.http.HttpPoster;
import org.junit.jupiter.api.Test;

class SpanBatchSenderFactoryTest {

  @Test
  void testBuilders() {
    HttpPoster h = (url, headers, body, mediaType) -> null;
    SpanBatchSenderFactory f = () -> h;

    assertNotNull(f.configureWith("abc123"));
    assertNotNull(f.createBatchSender("abc123"));
  }
}
