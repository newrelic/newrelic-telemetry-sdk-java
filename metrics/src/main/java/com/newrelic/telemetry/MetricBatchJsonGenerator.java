/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import static java.lang.Double.isFinite;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricBatchJsonGenerator {

  private static final Logger logger = LoggerFactory.getLogger(MetricJsonGenerator.class);

  private final MetricJsonGenerator metricJsonGenerator;
  private final AttributesJson attributesJson;

  public MetricBatchJsonGenerator(
      MetricJsonGenerator metricJsonGenerator, AttributesJson attributesJson) {
    this.metricJsonGenerator = metricJsonGenerator;
    this.attributesJson = attributesJson;
  }

  public String generateJson(MetricBatch metricBatch) {
    logger.debug("Generating json for metric batch.");
    StringBuilder builder = new StringBuilder();

    builder.append("[").append("{");

    addCommonBlock(metricBatch, builder);

    addMetrics(metricBatch, builder);

    builder.append("}").append("]");
    return builder.toString();
  }

  private void addMetrics(MetricBatch metricBatch, StringBuilder builder) {
    builder.append("\"metrics\":").append("[");
    Collection<Metric> metrics = metricBatch.getMetrics();

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
        metricJsonGenerator::writeCountJson,
        metricJsonGenerator::writeGaugeJson,
        metricJsonGenerator::writeSummaryJson);
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

  private void addCommonBlock(MetricBatch metricBatch, StringBuilder builder) {
    if (!metricBatch.getCommonAttributes().asMap().isEmpty()) {
      builder
          .append("\"common\":")
          .append("{")
          .append("\"attributes\":")
          .append(writeAttributes(metricBatch))
          .append("},");
    }
  }

  private String writeAttributes(MetricBatch metricBatch) {
    return attributesJson.toJson(metricBatch.getCommonAttributes().asMap());
  }
}
