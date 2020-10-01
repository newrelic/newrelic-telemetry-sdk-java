/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static java.util.Collections.unmodifiableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * A collection of key-value pairs that can be used as the dimensional attributes for metrics.
 *
 * <p>Only String keys are allowed. Acceptable values for attributes include Numbers, Strings, and
 * booleans.
 */
public class Attributes {
  private final Map<String, Object> rawAttributes = new HashMap<>();

  /** Creates an empty object */
  public Attributes() {}

  /** Creates a copy. Changes to the new copy will not affect the original and vice versa. */
  public Attributes(Attributes original) {
    rawAttributes.putAll(original.rawAttributes);
  }

  /** Creates a copy. Changes to the new copy will not affect the original and vice versa. */
  public Attributes copy() {
    return new Attributes(this);
  }

  public Attributes putAll(Attributes incoming) {
    rawAttributes.putAll(incoming.rawAttributes);
    return this;
  }

  /**
   * Add a string-valued attribute.
   *
   * @return this
   */
  public Attributes put(String key, String value) {
    rawAttributes.put(key, value);
    return this;
  }

  /**
   * Add a Number-valued attribute.
   *
   * @return this
   */
  public Attributes put(String key, Number value) {
    rawAttributes.put(key, value);
    return this;
  }

  /**
   * Add a boolean-valued attribute.
   *
   * @return this
   */
  public Attributes put(String key, boolean value) {
    rawAttributes.put(key, value);
    return this;
  }

  /**
   * Make a copy of these attributes.
   *
   * @return An unmodifiable copy of these attributes, as a Map.
   */
  public Map<String, Object> asMap() {
    return unmodifiableMap(new HashMap<>(rawAttributes));
  }

  /** @return true if there are no attributes in this Attributes instance */
  public boolean isEmpty() {
    return rawAttributes.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Attributes that = (Attributes) o;

    return rawAttributes != null
        ? rawAttributes.equals(that.rawAttributes)
        : that.rawAttributes == null;
  }

  @Override
  public int hashCode() {
    return rawAttributes != null ? rawAttributes.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "Attributes{" + "rawAttributes=" + rawAttributes + '}';
  }
}
