package com.newrelic.telemetry.events;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Telemetry;

/** An Event is ad hoc data structure, recorded in the New Relic Metric API. */
public final class Event implements Telemetry {
  private final String eventType;
  private final Attributes attributes;
  private final long timestamp; // in epoch ms

  public Event(String eventType, Attributes attributes, long timestamp) {
    this.eventType = eventType;
    this.attributes = attributes;
    this.timestamp = timestamp;
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
