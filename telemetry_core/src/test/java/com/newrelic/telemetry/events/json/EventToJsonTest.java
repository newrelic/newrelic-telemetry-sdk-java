package com.newrelic.telemetry.events.json;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.events.Event;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;


class EventToJsonTest {

    @Test
    public void testSimpleEvent() throws Exception {
        long eventTimeMillis = 888777666222L;
        Event event = new Event("jitThing", new Attributes().put("foo", "bar").put("bar", "baz"), eventTimeMillis);
        String expected = "{\"eventType\":\"jitThing\",\"sdkEventTimeMillis\":888777666222,\"foo\":\"bar\",\"bar\":\"baz\"}";

        EventToJson eventToJson = new EventToJson();

        String result = eventToJson.apply(event);

        assertEquals(expected, result);
    }

}