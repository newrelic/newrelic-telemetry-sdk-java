package com.newrelic.telemetry;

import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public interface AttributesJson {

  /**
   * These attributes will be put in the common block, and this must be formatted like this to be
   * accepted by the New Relic backend.
   *
   * <pre>
   *   {
   *     "stringKey": "StringValue",
   *     "numberKey": 5555.88,
   *     "booleanKey": true
   *   }
   * </pre>
   */
  String toJson(Map<String, Object> attributes);

  /**
   * Call this to remove illegal (NaN, -Infinity, null, etc) values from the attributes before
   * generating JSON for them.
   */
  default Map<String, Object> filterIllegalValues(Map<String, Object> attributes) {
    return attributes
        .entrySet()
        .stream()
        .filter(entry -> entry.getValue() != null)
        .filter(
            entry ->
                !(entry.getValue() instanceof Number)
                    || (Double.isFinite(((Number) entry.getValue()).doubleValue())))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }
}
