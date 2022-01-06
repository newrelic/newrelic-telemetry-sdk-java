/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.telemetry.events;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Telemetry;

/**
 * An Event is ad hoc set of key-value pairs with an associated timestamp, recorded in the New Relic
 * Metric API.
 */
public final class Event implements Telemetry {
  private final String eventType;
  private final Attributes attributes;
  private final long timestamp; // in epoch ms

  /**
   * Create an event with the given timestamp as the timestamp, in ms since unix epoch.
   *
   * @param eventType The type of event.
   * @param attributes The key-value pairs that make up the event.
   * @param timestamp time stamp in epoch milliseconds
   */
  public Event(String eventType, Attributes attributes, long timestamp) {
    if (eventType == null || "".equals(eventType)) {
      throw new IllegalArgumentException("eventType must not be empty.");
    }
    this.eventType = eventType;
    this.attributes = attributes;
    this.timestamp = timestamp;
  }

  /**
   * Copy constructor for events.
   *
   * @param other The event to copy
   */
  public Event(Event other) {
    eventType = other.eventType;
    attributes = new Attributes(other.attributes);
    timestamp = other.timestamp;
  }

  /**
   * Create an event with System.currentTimeMillis as the timestamp.
   *
   * @param eventType The type of event.
   * @param attributes The key-value pairs that make up the event.
   */
  public Event(String eventType, Attributes attributes) {
    this(eventType, attributes, System.currentTimeMillis());
  }

  public String getEventType() {
    return eventType;
  }

  public Attributes getAttributes() {
    return attributes;
  }

  public long getTimestamp() {
    return timestamp;
  }

  @Override
  public String toString() {
    return "Event{"
        + "eventType='"
        + eventType
        + '\''
        + ", attributes="
        + attributes
        + ", timestamp="
        + timestamp
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Event event = (Event) o;

    if (getTimestamp() != event.getTimestamp()) return false;
    if (getEventType() != null
        ? !getEventType().equals(event.getEventType())
        : event.getEventType() != null) return false;
    return getAttributes() != null
        ? getAttributes().equals(event.getAttributes())
        : event.getAttributes() == null;
  }

  @Override
  public int hashCode() {
    int result = getEventType() != null ? getEventType().hashCode() : 0;
    result = 31 * result + (getAttributes() != null ? getAttributes().hashCode() : 0);
    result = 31 * result + (int) (getTimestamp() ^ (getTimestamp() >>> 32));
    return result;
  }
}
