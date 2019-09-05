/*
 * ---------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.metrics.json;

import static java.lang.Double.isFinite;

import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.Metric;
import com.newrelic.telemetry.metrics.MetricBatch;
import com.newrelic.telemetry.metrics.Summary;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MetricBatchJsonTelemetryBlockWriter {

  private final MetricToJson metricToJson;

  public MetricBatchJsonTelemetryBlockWriter(MetricToJson metricToJson) {
    this.metricToJson = metricToJson;
  }

  public void appendTelemetryJson(MetricBatch batch, StringBuilder builder) {
    builder.append("\"metrics\":").append("[");
    Collection<Metric> metrics = batch.getTelemetry();

    builder.append(
        metrics
            .stream()
            .filter(this::isValid)
            .map(this::toJsonString)
            .collect(Collectors.joining(",")));

    builder.append("]");
  }

  private boolean isValid(Metric metric) {
    return typeDispatch(
        metric,
        count -> isFinite((count.getValue())),
        gauge -> isFinite((gauge.getValue())),
        summary ->
            isFinite(summary.getMax()) && isFinite(summary.getMin()) && isFinite(summary.getSum()));
  }

  private String toJsonString(Metric metric) {
    return typeDispatch(
        metric,
        metricToJson::writeCountJson,
        metricToJson::writeGaugeJson,
        metricToJson::writeSummaryJson);
  }

  private <T> T typeDispatch(
      Metric metric,
      Function<Count, T> countFunction,
      Function<Gauge, T> gaugeFunction,
      Function<Summary, T> summaryFunction) {
    if (metric instanceof Count) {
      return countFunction.apply((Count) metric);
    }
    if (metric instanceof Gauge) {
      return gaugeFunction.apply((Gauge) metric);
    }
    if (metric instanceof Summary) {
      return summaryFunction.apply((Summary) metric);
    }
    throw new UnsupportedOperationException("Unknown metric type: " + metric.getClass());
  }
}
