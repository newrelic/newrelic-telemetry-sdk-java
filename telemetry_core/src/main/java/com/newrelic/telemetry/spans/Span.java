package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.Telemetry;

/** TODO: Document me */
public class Span implements Telemetry {

  @Override
  public Type getType() {
    return Type.SPAN;
  }
}
