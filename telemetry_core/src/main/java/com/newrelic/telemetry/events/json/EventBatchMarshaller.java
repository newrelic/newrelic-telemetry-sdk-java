package com.newrelic.telemetry.events.json;

import com.newrelic.telemetry.events.EventBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBatchMarshaller {

  private static final Logger logger = LoggerFactory.getLogger(EventBatchMarshaller.class);
  private final EventBatchJsonCommonBlockWriter commonBlockWriter;
  private final EventBatchJsonTelemetryBlockWriter telemetryBlockWriter;

  public EventBatchMarshaller(
      EventBatchJsonCommonBlockWriter commonBlockWriter,
      EventBatchJsonTelemetryBlockWriter telemetryBlockWriter) {
    this.commonBlockWriter = commonBlockWriter;
    this.telemetryBlockWriter = telemetryBlockWriter;
  }

  public String toJson(EventBatch batch) {
    logger.debug("Generating json for event batch.");
    StringBuilder builder = new StringBuilder();

    builder.append("[");

    //    int lengthBefore = builder.length();
    //    commonBlockWriter.appendCommonJson(batch, builder);
    //    if (builder.length() > lengthBefore) {
    //      builder.append(",");
    //    }
    telemetryBlockWriter.appendTelemetryJson(batch, builder);

    builder.append("]");
    logger.debug(builder.toString());
    return builder.toString();
  }
}
