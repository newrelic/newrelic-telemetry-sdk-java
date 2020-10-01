/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics.json;

import static java.lang.Double.isFinite;

import com.newrelic.telemetry.metrics.*;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    AtomicInteger retainedCount = new AtomicInteger();
    builder.append(
        metrics
            .stream()
            .filter(this::isValid)
            .map(this::toJsonString)
            .peek(x -> retainedCount.getAndIncrement())
            .collect(Collectors.joining(",")));

    if (retainedCount.get() != metrics.size()) {
      logger.info(
          "Dropped "
              + (metrics.size() - retainedCount.get())
              + " metrics from batch due to invalid metric contents (you should fix this)");
      logAllInvalid(metrics);
    }
    builder.append("]");
  }

  private void logAllInvalid(Collection<Metric> metrics) {
    metrics.stream().filter(this::isInvalid).forEach(this::logInvalid);
  }

  private void logInvalid(Metric invalidMetric) {
    logger.debug(
        "  * Dropped "
            + typeDispatch(
                invalidMetric,
                count -> "Count(name=" + count.getName() + ",  value = " + count.getValue() + ")",
                gauge -> "Gauge(name=" + gauge.getName() + ", value = " + gauge.getValue() + ")",
                summary -> "Summary(name=" + summary.getName() + ", value = " + summary.getSum()));
  }

  private boolean isInvalid(Metric metric) {
    return !isValid(metric);
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
