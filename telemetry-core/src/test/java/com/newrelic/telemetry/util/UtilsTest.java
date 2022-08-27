package com.newrelic.telemetry.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class UtilsTest {
  @Test
  @DisplayName("Utils.generateUUID() generates V4 UUID")
  void testGenerateUUID() throws Exception {
    final UUID uuid = Utils.generateUUID();

    assertEquals(4, uuid.version()); // version 4 as per RFC 4122
    assertEquals(2, uuid.variant()); // variant 2 as per RFC 4122

    // UUID.randomUUID() generates V4 UUIDs
    final UUID referenceUUID = UUID.randomUUID();
    assertEquals(referenceUUID.version(), uuid.version());
    assertEquals(referenceUUID.variant(), uuid.variant());
  }
}
