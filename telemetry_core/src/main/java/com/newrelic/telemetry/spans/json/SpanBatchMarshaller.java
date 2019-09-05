package com.newrelic.telemetry.spans.json;

import com.newrelic.telemetry.spans.SpanBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpanBatchMarshaller {

  private static final Logger logger = LoggerFactory.getLogger(SpanBatchMarshaller.class);
  private final SpanJsonCommonBlockWriter commonBlockWriter;
  private final SpanJsonTelemetryBlockWriter telemetryBlockWriter;

  public SpanBatchMarshaller(SpanJsonCommonBlockWriter commonBlockWriter,
      SpanJsonTelemetryBlockWriter telemetryBlockWriter) {
    this.commonBlockWriter = commonBlockWriter;
    this.telemetryBlockWriter = telemetryBlockWriter;
  }

  public String toJson(SpanBatch batch) {
    logger.debug("Generating json for span batch.");
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
