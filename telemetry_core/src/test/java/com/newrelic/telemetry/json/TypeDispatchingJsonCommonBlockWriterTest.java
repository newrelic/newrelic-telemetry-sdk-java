/*
 * ---------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.json;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.Telemetry.Type;
import com.newrelic.telemetry.TelemetryBatch;
import com.newrelic.telemetry.metrics.Metric;
import com.newrelic.telemetry.spans.Span;
import org.junit.jupiter.api.Test;

class TypeDispatchingJsonCommonBlockWriterTest {

  @Test
  void testMetrics() {
    StringBuilder sb = new StringBuilder();
    TelemetryBatch<Metric> batch = mock(TelemetryBatch.class);
    JsonCommonBlockWriter metricsWriter = mock(JsonCommonBlockWriter.class);

    when(batch.getType()).thenReturn(Type.METRIC);

    TypeDispatchingJsonCommonBlockWriter testClass =
        new TypeDispatchingJsonCommonBlockWriter(metricsWriter, null);

    testClass.appendCommonJson(batch, sb);
    verify(metricsWriter).appendCommonJson(batch, sb);
  }

  @Test
  void testSpans() {
    StringBuilder sb = new StringBuilder();
    TelemetryBatch<Span> batch = mock(TelemetryBatch.class);
    JsonCommonBlockWriter spansWriter = mock(JsonCommonBlockWriter.class);

    when(batch.getType()).thenReturn(Type.SPAN);

    TypeDispatchingJsonCommonBlockWriter testClass =
        new TypeDispatchingJsonCommonBlockWriter(null, spansWriter);

    testClass.appendCommonJson(batch, sb);
    verify(spansWriter).appendCommonJson(batch, sb);
  }
}
