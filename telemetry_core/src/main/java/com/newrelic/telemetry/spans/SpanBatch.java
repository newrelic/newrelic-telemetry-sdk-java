package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Telemetry.Type;
import com.newrelic.telemetry.TelemetryBatch;
import java.util.Collection;
import lombok.Value;

@Value
public class SpanBatch extends TelemetryBatch<Span> {

  private final String traceId;

  public SpanBatch(Collection<Span> telemetry, Attributes commonAttributes,
      String traceId) {
    super(Type.SPAN, telemetry, commonAttributes);
    this.traceId = traceId;
  }
}
