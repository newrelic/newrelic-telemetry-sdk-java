/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics.json;

import com.newrelic.telemetry.metrics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Double.isFinite;

public class MetricBatchJsonTelemetryBlockWriter {

  private static final Logger logger =
      LoggerFactory.getLogger(MetricBatchJsonTelemetryBlockWriter.class);
  private final MetricToJson metricToJson;

  public MetricBatchJsonTelemetryBlockWriter(MetricToJson metricToJson) {
    this.metricToJson = metricToJson;
  }

  public void appendTelemetryJson(MetricBatch batch, StringBuilder builder) {
    builder.append("\"metrics\":").append("[");
    Collection<Metric> metrics = batch.getTelemetry();

    AtomicInteger filteredCount = new AtomicInteger();
    builder.append(
        metrics
            .stream()
            .filter(this::isValid)
            .map(this::toJsonString)
            .peek(x -> filteredCount.getAndIncrement())
            .collect(Collectors.joining(",")));

    if (filteredCount.get() != metrics.size()) {
      logger.debug(
          "Dropped "
              + (metrics.size() - filteredCount.get())
              + " metrics from batch due to invalid metric contents (you should fix this)");
    }
    builder.append("]");
  }

  private boolean isValid(Metric metric) {
    return typeDispatch(
        metric,
        count -> isFinite((count.getValue())),
        gauge -> isFinite((gauge.getValue())),
        summary -> isFinite(summary.getSum()));
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
