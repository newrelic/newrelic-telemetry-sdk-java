package com.newrelic.telemetry.events.json;

import com.google.gson.stream.JsonWriter;
import com.newrelic.telemetry.events.Event;
import com.newrelic.telemetry.json.AttributesJson;
import java.io.IOException;
import java.io.StringWriter;
import java.util.function.Function;

public class EventToJson implements Function<Event, String> {

  private final AttributesJson attributeJson = new AttributesJson();

  @Override
  public String apply(Event event) {
    try {
      StringWriter out = new StringWriter();
      JsonWriter jsonWriter = new JsonWriter(out);
      jsonWriter.beginObject();

      jsonWriter.name("eventType").value(event.getEventType());
      jsonWriter.name("timestamp").value(event.getTimestamp());

      String attributes = attributeJson.toJson(event.getAttributes().asMap());
      if (!attributes.isEmpty()) {
        jsonWriter.name("attributes").jsonValue(attributes);
      }

      jsonWriter.endObject();
      return out.toString();
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate summary json", e);
    }
  }
}
