package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Telemetry;
import lombok.Value;

/** TODO: Document me */
@Value
public class Span implements Telemetry {

  private final String id;
  private final Attributes attributes;

  @Override
  public Type getType() {
    return Type.SPAN;
  }
}
