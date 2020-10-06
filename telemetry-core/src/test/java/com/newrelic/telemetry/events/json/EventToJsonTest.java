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
            "jitThing",
            new Attributes()
                .put("foo", "bar")
                .put("bar", "baz")
                .put("quux", true)
                .put("zzz", 819)
                .put("yyy", 2.997),
            eventTimeMillis);
    String expected =
        "{\"eventType\":\"jitThing\", \"timestamp\":888777666222, \"foo\":\"bar\",\"bar\":\"baz\",\"quux\":true,\"zzz\":819,\"yyy\":2.997}";

    EventToJson eventToJson = new EventToJson();

    String json = eventToJson.apply(event);
    JSONAssert.assertEquals(expected, json, false);
  }
}
