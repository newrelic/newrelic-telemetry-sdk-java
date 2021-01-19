/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.newrelic.telemetry.core.BaseConfig;
import com.newrelic.telemetry.core.SenderConfiguration;
import com.newrelic.telemetry.core.SpanBatchSenderFactory;
import com.newrelic.telemetry.core.http.HttpPoster;
import org.junit.jupiter.api.Test;

class SpanBatchSenderFactoryTest {

  HttpPoster httpPoster = (url, headers, body, mediaType) -> null;
  SpanBatchSenderFactory factory = () -> httpPoster;

  @Test
  void testBuilders() {
    assertNotNull(factory.configureWith("abc123"));
    assertNotNull(factory.createBatchSender("abc123"));
  }

  @Test
  void withBaseConfig() {
    BaseConfig baseConfig = new BaseConfig("hey", true, "ttt");
    SenderConfiguration.SenderConfigurationBuilder builder = factory.configureWith(baseConfig);
    SenderConfiguration result = builder.build();
    assertEquals("hey", result.getApiKey());
    assertTrue(result.isAuditLoggingEnabled());
    assertEquals("ttt", result.getSecondaryUserAgent());
  }
}
