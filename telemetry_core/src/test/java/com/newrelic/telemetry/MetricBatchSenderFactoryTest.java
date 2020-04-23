/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.newrelic.telemetry.http.HttpPoster;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class MetricBatchSenderFactoryTest {

  @Test
  void testBuilders() throws Exception {
    HttpPoster h = (url, headers, body, mediaType) -> null;
    MetricBatchSenderFactory f = d -> h;

    assertNotNull(f.configureWith("abc123", ofSeconds(5)));
    assertNotNull(f.configureWith("abc123"));
    assertNotNull(f.createBatchSender("abc123"));
    assertNotNull(f.createBatchSender("abc123", Duration.ofSeconds(5)));
  }
}
