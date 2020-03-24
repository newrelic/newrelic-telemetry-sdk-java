package com.newrelic.telemetry.events.json;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.events.Event;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class EventToJsonTest {

  @Test
  public void testSimpleEvent() throws Exception {
    long eventTimeMillis = 888777666222L;
    Event event =
        new Event(
            "jitThing", new Attributes().put("foo", "bar").put("bar", "baz"), eventTimeMillis);
    String expected =
        "{\"eventType\":\"jitThing\", \"timestamp\":888777666222, \"attributes\": {\"foo\":\"bar\",\"bar\":\"baz\"}}";

    EventToJson eventToJson = new EventToJson();

    String json = eventToJson.apply(event);
    JSONAssert.assertEquals(expected, json, false);
  }
}
