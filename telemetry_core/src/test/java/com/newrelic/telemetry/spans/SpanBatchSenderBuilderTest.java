/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.spans;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.newrelic.telemetry.SenderConfiguration.SenderConfigurationBuilder;
import org.junit.jupiter.api.Test;

class SpanBatchSenderBuilderTest {

  @Test
  void testBuild() {
    SenderConfigurationBuilder dummy = new SenderConfigurationBuilder("http://fake.com", "/");
    dummy.apiKey("123");
    dummy.httpPoster((url, headers, body, mediaType) -> null);

    SpanBatchSender result = new SpanBatchSenderBuilder(dummy).build();
    assertNotNull(result);
  }

  @Test
  void testMissingApiKey() {
    SenderConfigurationBuilder dummy = new SenderConfigurationBuilder("http://fake.com", "/");
    dummy.httpPoster((url, headers, body, mediaType) -> null);

    assertThrows(IllegalArgumentException.class, () -> new SpanBatchSenderBuilder(dummy).build());
  }

  @Test
  void testMissingHttpPoster() {
    SenderConfigurationBuilder dummy = new SenderConfigurationBuilder("http://fake.com", "/");
    dummy.apiKey("123");

    assertThrows(IllegalArgumentException.class, () -> new SpanBatchSenderBuilder(dummy).build());
  }
}
