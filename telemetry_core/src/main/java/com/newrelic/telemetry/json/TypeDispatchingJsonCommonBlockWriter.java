/*
 * ---------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.json;

import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.TelemetryBatch;
import com.newrelic.telemetry.metrics.Metric;
import com.newrelic.telemetry.metrics.MetricBatch;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;

public class TypeDispatchingJsonCommonBlockWriter {

  private final JsonCommonBlockWriter<Metric, MetricBatch> commonBlockMetricsWriter;
  private final JsonCommonBlockWriter<Span, SpanBatch> commonBlockSpanWriter;

  public TypeDispatchingJsonCommonBlockWriter(
      JsonCommonBlockWriter<Metric, MetricBatch> commonBlockMetricsWriter,
      JsonCommonBlockWriter<Span, SpanBatch> commonBlockSpanWriter) {
    this.commonBlockMetricsWriter = commonBlockMetricsWriter;
    this.commonBlockSpanWriter = commonBlockSpanWriter;
  }

  public <S extends Telemetry, T extends TelemetryBatch<S>> void appendCommonJson(
      T batch, StringBuilder builder) {
    chooseCommonWriter(batch).appendCommonJson(batch, builder);
  }

  @SuppressWarnings("unchecked")
  private <S extends Telemetry, T extends TelemetryBatch<S>>
      JsonCommonBlockWriter<S, T> chooseCommonWriter(T batch) {
    switch (batch.getType()) {
      case METRIC:
        return (JsonCommonBlockWriter<S, T>) commonBlockMetricsWriter;
      case SPAN:
        return (JsonCommonBlockWriter<S, T>) commonBlockSpanWriter;
    }
    throw new UnsupportedOperationException("Unhandled telemetry batch type: " + batch.getType());
  }
}
