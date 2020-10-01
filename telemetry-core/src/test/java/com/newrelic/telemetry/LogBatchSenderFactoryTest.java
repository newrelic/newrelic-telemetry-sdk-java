package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.newrelic.telemetry.http.HttpPoster;
import org.junit.jupiter.api.Test;

class LogBatchSenderFactoryTest {

  HttpPoster httpPoster = (url, headers, body, mediaType) -> null;
  LogBatchSenderFactory factory = () -> httpPoster;

  @Test
  void testWithBaseConfig() {
    BaseConfig baseConfig = new BaseConfig("one", true, "twelve");
    SenderConfiguration.SenderConfigurationBuilder builder = factory.configureWith(baseConfig);
    SenderConfiguration result = builder.build();
    assertEquals("one", result.getApiKey());
    assertTrue(result.isAuditLoggingEnabled());
    assertEquals("twelve", result.getSecondaryUserAgent());
  }
}
