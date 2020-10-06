/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.logs.json;

import com.google.gson.stream.JsonWriter;
import com.newrelic.telemetry.logs.LogBatch;
import java.io.IOException;
import java.io.StringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogBatchMarshaller {

  private static final Logger logger = LoggerFactory.getLogger(LogBatchMarshaller.class);
  private final LogJsonCommonBlockWriter commonBlockWriter;
  private final LogJsonTelemetryBlockWriter telemetryBlockWriter;

  public LogBatchMarshaller(
      LogJsonCommonBlockWriter commonBlockWriter,
      LogJsonTelemetryBlockWriter telemetryBlockWriter) {
    this.commonBlockWriter = commonBlockWriter;
    this.telemetryBlockWriter = telemetryBlockWriter;
  }

  public String toJson(LogBatch batch) {
    logger.debug("Generating json for log batch.");

    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

    try {
      jsonWriter.beginArray().beginObject();
      commonBlockWriter.appendCommonJson(batch, jsonWriter);
      telemetryBlockWriter.appendTelemetryJson(batch, jsonWriter);
      jsonWriter.endObject().endArray();
    } catch (IOException e) {
      throw new RuntimeException("Failed to marshall json for a log batch");
    }

    return out.toString();
  }
}
