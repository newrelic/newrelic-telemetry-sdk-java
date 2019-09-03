package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.Telemetry;

/** Tag interface to identify spans. */
public class Span implements Telemetry {

  @Override
  public Type getType() {
    return Type.SPAN;
  }
}
