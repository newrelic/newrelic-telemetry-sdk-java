package com.newrelic.telemetry.json;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.Metric;
import com.newrelic.telemetry.Span;
import com.newrelic.telemetry.Telemetry.Type;
import com.newrelic.telemetry.TelemetryBatch;
import org.junit.jupiter.api.Test;

class TypeDispatchingJsonTelemetryBlockWriterTest {

  @Test
  void testMetrics() {
    StringBuilder sb = new StringBuilder();
    TelemetryBatch<Metric> batch = mock(TelemetryBatch.class);
    JsonTelemetryBlockWriter metricsWriter = mock(JsonTelemetryBlockWriter.class);

    when(batch.getType()).thenReturn(Type.METRIC);

    TypeDispatchingJsonTelemetryBlockWriter testClass =
        new TypeDispatchingJsonTelemetryBlockWriter(metricsWriter, null);

    testClass.appendTelemetry(batch, sb);
    verify(metricsWriter).appendTelemetry(batch, sb);
  }

  @Test
  void testSpans() {
    StringBuilder sb = new StringBuilder();
    TelemetryBatch<Span> batch = mock(TelemetryBatch.class);
    JsonTelemetryBlockWriter spansWriter = mock(JsonTelemetryBlockWriter.class);

    when(batch.getType()).thenReturn(Type.SPAN);

    TypeDispatchingJsonTelemetryBlockWriter testClass =
        new TypeDispatchingJsonTelemetryBlockWriter(null, spansWriter);

    testClass.appendTelemetry(batch, sb);
    verify(spansWriter).appendTelemetry(batch, sb);
  }
}
