/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.spans;

import static com.newrelic.telemetry.spans.SimpleSpanBatchSender.*;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.newrelic.telemetry.http.HttpPoster;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class SimpleSpanBatchSenderTest {

  @Test
  void testBuilders() throws Exception {
    HttpPoster dummyPoster = (url, headers, body, mediaType) -> null;
    provider(duration -> dummyPoster);

    assertNotNull(builder("abc123", ofSeconds(5)));
    assertNotNull(builder("abc123"));
    assertNotNull(build("abc123"));
    assertNotNull(build("abc123", Duration.ofSeconds(5)));
  }
}
