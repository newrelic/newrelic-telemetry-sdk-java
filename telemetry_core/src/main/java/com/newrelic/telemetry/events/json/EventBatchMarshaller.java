package com.newrelic.telemetry.events.json;

import com.newrelic.telemetry.events.EventBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBatchMarshaller {

  private static final Logger logger = LoggerFactory.getLogger(EventBatchMarshaller.class);

  public String toJson(EventBatch batch) {
    logger.debug("Generating json for event batch.");
    StringBuilder builder = new StringBuilder();

    builder.append("[").append("{");

    //        int lengthBefore = builder.length();
    //        commonBlockWriter.appendCommonJson(batch, builder);
    //        if (builder.length() > lengthBefore) {
    //            builder.append(",");
    //        }
    //        telemetryBlockWriter.appendTelemetryJson(batch, builder);

    builder.append("}").append("]");
    return builder.toString();
  }
}
