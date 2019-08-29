/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */
package com.newrelic.telemetry;

import com.google.gson.Gson;
import com.google.gson.stream.JsonWriter;
import com.newrelic.telemetry.json.AttributesJson;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

public class AttributesGson implements AttributesJson {

  private final Gson gson;

  public AttributesGson(Gson gson) {
    this.gson = gson;
  }

  @Override
  public String toJson(Map<String, Object> attributes) {
    StringWriter out = new StringWriter();
    Map<String, Object> filteredAttributes = filterIllegalValues(attributes);
    filterIllegalValues(filteredAttributes);
    if (filteredAttributes.isEmpty()) {
      return "{}";
    }
    try {
      JsonWriter jsonWriter = gson.newJsonWriter(out);
      jsonWriter.beginObject();

      for (Map.Entry<String, Object> attribute : filteredAttributes.entrySet()) {
        Object value = attribute.getValue();

        if (value instanceof Boolean) {
          jsonWriter.name(attribute.getKey()).value((boolean) value);
        } else if (value instanceof Number) {
          Number num = (Number) value;
          jsonWriter.name(attribute.getKey()).value(num);
        } else {
          jsonWriter.name(attribute.getKey()).value(String.valueOf(value));
        }
      }
      jsonWriter.endObject();
    } catch (IOException e) {
      throw new RuntimeException("Failed to generate attributes json");
    }
    return out.toString();
  }
}
