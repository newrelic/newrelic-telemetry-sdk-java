package com.newrelic.telemetry.events;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Telemetry;
import lombok.Value;

/** An Event is ad hoc data structure, recorded in the New Relic Metric API. */
@Value
public class Event implements Telemetry {
  private final String id;
  private final Attributes attributes;

  private final long timestamp; // in epoch ms

  private final String serviceName; // service.name <- goes in attributes
  private final String name; // goes in attributes
}
