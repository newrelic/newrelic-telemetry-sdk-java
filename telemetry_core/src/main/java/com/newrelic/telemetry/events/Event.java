package com.newrelic.telemetry.events;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Telemetry;
import lombok.Value;

/** An Event is ad hoc data structure, recorded in the New Relic Metric API. */
@Value
public class Event implements Telemetry {

  String eventType;
  Attributes attributes;
  long timestamp; // in epoch ms
}
