package com.newrelic.telemetry.core.events;

import com.newrelic.telemetry.core.Attributes;
import com.newrelic.telemetry.core.TelemetryBatch;
import java.util.Collection;

/** Represents a set of {@link Event} instances, to be sent up to the New Relic Metrics API. */
public class EventBatch extends TelemetryBatch<Event> {

  public EventBatch(Collection<Event> events, Attributes commonAttributes) {
    super(events, commonAttributes);
  }

  public EventBatch(Collection<Event> events) {
    super(events, new Attributes());
  }

  @Override
  public TelemetryBatch<Event> createSubBatch(Collection<Event> telemetry) {
    return new EventBatch(telemetry, getCommonAttributes());
  }

  @Override
  public String toString() {
    return "EventBatch{} " + super.toString();
  }
}
