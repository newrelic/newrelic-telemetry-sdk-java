package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Telemetry;
import lombok.Builder;
import lombok.Value;

/** TODO: Document me */
@Value
@Builder
public final class Span implements Telemetry {

  private final String id;
  private final Attributes attributes;

  private String traceId = ""; // trace.id <- top level
  private long timestamp = 0; // in epoch ms

  private String serviceName = ""; // service.name <- goes in attributes
  private double durationMs = 0; // duration.ms <- goes in attributes
  private String name = ""; // goes in attributes
  private String parentId = ""; // parent.id <- goes in attributes

  public Span(String id, Attributes attributes) {
    this.id = id;
    this.attributes = attributes;
  }

  @Override
  public Type getType() {
    return Type.SPAN;
  }
}
