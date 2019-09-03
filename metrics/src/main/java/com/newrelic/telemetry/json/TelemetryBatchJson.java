/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.json;

import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.TelemetryBatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Class that converts any type of telemetry batch into the appropriate API json */
public class TelemetryBatchJson {

  private static final Logger logger = LoggerFactory.getLogger(TelemetryBatchJson.class);
  private final TypeDispatchingJsonCommonBlockWriter commonBlockWriter;
  private final TypeDispatchingJsonTelemetryBlockWriter mainBodyWriter;

  public TelemetryBatchJson(
      TypeDispatchingJsonCommonBlockWriter commonBlockWriter,
      TypeDispatchingJsonTelemetryBlockWriter mainBodyWriter) {
    this.commonBlockWriter = commonBlockWriter;
    this.mainBodyWriter = mainBodyWriter;
  }

  public <T extends Telemetry> String toJson(TelemetryBatch<T> batch) {
    logger.debug("Generating json for telemetry batch.");
    StringBuilder builder = new StringBuilder();

    builder.append("[").append("{");

    int lengthBefore = builder.length();
    commonBlockWriter.appendCommonJson(batch, builder);
    if (builder.length() > lengthBefore) {
      builder.append(",");
    }
    mainBodyWriter.appendTelemetry(batch, builder);

    builder.append("}").append("]");
    return builder.toString();
  }
}
