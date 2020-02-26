package com.newrelic.telemetry.events;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.TelemetryBatch;
import java.util.Collection;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Represents a set of {@link Event} instances, to be sent up to the New Relic Metrics API. */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class EventBatch extends TelemetryBatch<Event> {

  public EventBatch(Collection<Event> events, Attributes commonAttributes) {
    super(events, commonAttributes);
  }

  @Override
  public TelemetryBatch<Event> createSubBatch(Collection<Event> telemetry) {
    return new EventBatch(telemetry, getCommonAttributes());
  }
}
