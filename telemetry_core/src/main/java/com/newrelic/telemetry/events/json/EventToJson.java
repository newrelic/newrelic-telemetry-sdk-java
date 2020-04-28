/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.telemetry.events.json;

import com.newrelic.telemetry.events.Event;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import java.util.function.Function;
import vendored.com.google.gson.stream.JsonWriter;

public class EventToJson implements Function<Event, String> {

  @Override
  public String apply(Event event) {
    try {
      StringWriter out = new StringWriter();
      JsonWriter jsonWriter = new JsonWriter(out);
      jsonWriter.beginObject();

      jsonWriter.name("eventType").value(event.getEventType());
      jsonWriter.name("timestamp").value(event.getTimestamp());

      for (Map.Entry<String, Object> entry : event.getAttributes().asMap().entrySet()) {
        Object value = entry.getValue();
        if (value instanceof String) {
          String sValue = (String) value;
          jsonWriter.name(entry.getKey()).value(sValue);
        } else if (value instanceof Number) {
          Number nValue = (Number) value;
          jsonWriter.name(entry.getKey()).value(nValue);
        } else if (value instanceof Boolean) {
          Boolean bValue = (Boolean) value;
          jsonWriter.name(entry.getKey()).value(bValue);
        } else {
          throw new RuntimeException(
              String.format(
                  "Failed to generate json type %s encountered with value %s",
                  value.getClass(), value));
        }
      }

      jsonWriter.endObject();
      return out.toString();
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate summary json", e);
    }
  }
}
