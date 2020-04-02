package com.newrelic.telemetry;

import static com.newrelic.telemetry.SimpleSpanBatchSender.*;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class SimpleSpanBatchSenderTest {
  @Test
  void testBuilders() throws Exception {
    assertNotNull(builder("abc123", ofSeconds(5)));
    assertNotNull(builder("abc123"));
    assertNotNull(build("abc123"));
    assertNotNull(build("abc123", Duration.ofSeconds(5)));
  }
}
