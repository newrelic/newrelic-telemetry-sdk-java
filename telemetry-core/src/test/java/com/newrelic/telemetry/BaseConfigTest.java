package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.*;

import com.newrelic.telemetry.core.BaseConfig;
import org.junit.jupiter.api.Test;

class BaseConfigTest {

  @Test
  void defaultAuditModeIsDisabled() {
    assertFalse(new BaseConfig("1").isAuditLoggingEnabled());
  }

  @Test
  void defaultSecondaryUserAgentIsNull() {
    assertNull(new BaseConfig("1").getSecondaryUserAgent());
    assertNull(new BaseConfig("1", true).getSecondaryUserAgent());
  }
}
