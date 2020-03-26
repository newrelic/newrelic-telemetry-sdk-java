/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.TelemetryBatch;
import java.util.Collection;
import java.util.Optional;

/** Represents a set of {@link Span} instances, to be sent up to the New Relic Trace API. */
// @EqualsAndHashCode(callSuper = true)
// @ToString(callSuper = true)
public class SpanBatch extends TelemetryBatch<Span> {

  private final String traceId;

  public SpanBatch(Collection<Span> telemetry, Attributes commonAttributes) {
    this(telemetry, commonAttributes, null);
  }

  @Override
  public TelemetryBatch<Span> createSubBatch(Collection<Span> telemetry) {
    return new SpanBatch(telemetry, getCommonAttributes(), traceId);
  }

  public SpanBatch(Collection<Span> telemetry, Attributes commonAttributes, String traceId) {
    super(telemetry, commonAttributes);
    this.traceId = traceId;
  }

  public Optional<String> getTraceId() {
    return Optional.ofNullable(traceId);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    SpanBatch spanBatch = (SpanBatch) o;

    return getTraceId() != null
        ? getTraceId().equals(spanBatch.getTraceId())
        : spanBatch.getTraceId() == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (getTraceId() != null ? getTraceId().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "SpanBatch{" + "traceId='" + traceId + '\'' + "} " + super.toString();
  }
}
