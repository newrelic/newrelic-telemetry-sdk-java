package com.newrelic.telemetry.core.events;

import static org.junit.jupiter.api.Assertions.assertThrows;

import com.newrelic.telemetry.core.Attributes;
import org.junit.jupiter.api.Test;

public class EventTest {

  @Test
  void testNullEventType() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new Event(
              new Event(null, new Attributes().put("key1", "val1"), System.currentTimeMillis()));
        });
  }

  @Test
  void testEmptyStringEventType() {
    assertThrows(
        IllegalArgumentException.class,
        () -> {
          new Event(
              new Event("", new Attributes().put("key2", "val2"), System.currentTimeMillis()));
        });
  }
}
