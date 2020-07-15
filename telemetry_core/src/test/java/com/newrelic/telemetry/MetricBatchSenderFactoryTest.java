/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.newrelic.telemetry.http.HttpPoster;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetricBatchSenderFactoryTest {

  MetricBatchSenderFactory factory;

  @BeforeEach
  void setup() {
    HttpPoster httpPoster = (url, headers, body, mediaType) -> null;
    factory = () -> httpPoster;
  }

  @Test
  void testBuilders() {
    assertNotNull(factory.configureWith("abc123"));
    assertNotNull(factory.createBatchSender("abc123"));
  }

  @Test
  void configureWithBaseConfig() {
    BaseConfig baseConfig = new BaseConfig("123", true, "flibber");
    SenderConfiguration.SenderConfigurationBuilder builder = factory.configureWith(baseConfig);
    SenderConfiguration result = builder.build();
    assertEquals("123", result.getApiKey());
    assertTrue(result.isAuditLoggingEnabled());
    assertEquals("flibber", result.getSecondaryUserAgent());
  }
}
