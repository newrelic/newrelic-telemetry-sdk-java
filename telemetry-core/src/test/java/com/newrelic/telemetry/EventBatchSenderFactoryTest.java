package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.newrelic.telemetry.http.HttpPoster;
import org.junit.jupiter.api.Test;

class EventBatchSenderFactoryTest {

  HttpPoster httpPoster = (url, headers, body, mediaType) -> null;
  EventBatchSenderFactory factory = () -> httpPoster;

  @Test
  void testWithBaseConfig() {
    BaseConfig baseConfig = new BaseConfig("a", false, "b");
    SenderConfiguration.SenderConfigurationBuilder builder = factory.configureWith(baseConfig);
    SenderConfiguration result = builder.build();
    assertEquals("a", result.getApiKey());
    assertFalse(result.isAuditLoggingEnabled());
    assertEquals("b", result.getSecondaryUserAgent());
  }
}
