/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.spans.json;

import com.google.gson.stream.JsonWriter;
import com.newrelic.telemetry.spans.SpanBatch;
import java.io.IOException;
import java.io.StringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpanBatchMarshaller {

  private static final Logger logger = LoggerFactory.getLogger(SpanBatchMarshaller.class);
  private final SpanJsonCommonBlockWriter commonBlockWriter;
  private final SpanJsonTelemetryBlockWriter telemetryBlockWriter;

  public SpanBatchMarshaller(
      SpanJsonCommonBlockWriter commonBlockWriter,
      SpanJsonTelemetryBlockWriter telemetryBlockWriter) {
    this.commonBlockWriter = commonBlockWriter;
    this.telemetryBlockWriter = telemetryBlockWriter;
  }

  public String toJson(SpanBatch batch) {
    logger.debug("Generating json for span batch.");

    StringWriter out = new StringWriter();
    JsonWriter jsonWriter = new JsonWriter(out);

    try {
      jsonWriter.beginArray().beginObject();
      commonBlockWriter.appendCommonJson(batch, jsonWriter);
      telemetryBlockWriter.appendTelemetryJson(batch, jsonWriter);
      jsonWriter.endObject().endArray();
    } catch (IOException e) {
      throw new RuntimeException("Failed to marshall json for a span batch");
    }

    return out.toString();
  }
}
