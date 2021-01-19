package com.newrelic.telemetry.core.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.newrelic.telemetry.core.Attributes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventBufferTest {
  @Test
  @DisplayName("Get size of Events")
  void testMetricsSize() {
    long currentTimeMillis = 350;
    EventBuffer eventBuffer = new EventBuffer(new Attributes());
    Event expectedEvent =
        new Event("myEvent", new Attributes().put("key1", "val1"), currentTimeMillis);
    assertEquals(0, eventBuffer.size());
    eventBuffer.addEvent(expectedEvent);
    assertEquals(1, eventBuffer.size());
  }
}
