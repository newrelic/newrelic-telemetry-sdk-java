package com.newrelic.telemetry.metrics.json;

import com.newrelic.telemetry.metrics.MetricBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricBatchMarshaller {

  private static final Logger logger = LoggerFactory.getLogger(MetricBatchMarshaller.class);
  private final MetricBatchJsonCommonBlockWriter commonBlockWriter;
  private final MetricBatchJsonTelemetryBlockWriter telemetryBlockWriter;

  public MetricBatchMarshaller(MetricBatchJsonCommonBlockWriter commonBlockWriter,
      MetricBatchJsonTelemetryBlockWriter telemetryBlockWriter) {
    this.commonBlockWriter = commonBlockWriter;
    this.telemetryBlockWriter = telemetryBlockWriter;
  }

  public String toJson(MetricBatch batch) {
    logger.debug("Generating json for metric batch.");
    StringBuilder builder = new StringBuilder();

    builder.append("[").append("{");

    int lengthBefore = builder.length();
    commonBlockWriter.appendCommonJson(batch, builder);
    if (builder.length() > lengthBefore) {
      builder.append(",");
    }
    telemetryBlockWriter.appendTelemetryJson(batch, builder);

    builder.append("}").append("]");
    return builder.toString();
  }

}
