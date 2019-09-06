/*
 * ---------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.TelemetryBatch;
import java.util.Collection;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/** Represents a set of {@link Span} instances, to be sent up to the New Relic Metrics API. */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SpanBatch extends TelemetryBatch<Span> {

  private final String traceId;

  public SpanBatch(Collection<Span> telemetry, Attributes commonAttributes) {
    this(telemetry, commonAttributes, null);
  }

  public SpanBatch(Collection<Span> telemetry, Attributes commonAttributes, String traceId) {
    super(telemetry, commonAttributes);
    this.traceId = traceId;
  }

  public Optional<String> getTraceId() {
    return Optional.ofNullable(traceId);
  }
}
