package com.newrelic.telemetry;

/** Telemetry is the root type for all pieces of telemetry data. */
public interface Telemetry {

  Type getType();

  static enum Type {
    METRIC,
    SPAN
  }
}
