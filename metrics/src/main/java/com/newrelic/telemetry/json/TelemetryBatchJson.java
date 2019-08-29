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

/** Class that convert any type of telemetry batch into the appropriate API json */
public class TelemetryBatchJson {

  private static final Logger logger = LoggerFactory.getLogger(MetricJsonGenerator.class);
  private final JsonCommonBlockWriter commonBlockWriter;
  private final JsonTelemetryBlockWriter mainBodyWriter;

  public TelemetryBatchJson(
      JsonCommonBlockWriter commonBlockWriter, JsonTelemetryBlockWriter mainBodyWriter) {
    this.commonBlockWriter = commonBlockWriter;
    this.mainBodyWriter = mainBodyWriter;
  }

  public <T extends Telemetry> String toJson(TelemetryBatch<T> batch) {
    logger.debug("Generating json for telemetry batch.");
    StringBuilder builder = new StringBuilder();

    builder.append("[").append("{");

    commonBlockWriter.appendCommonJson(batch, builder);
    builder.append(",");
    mainBodyWriter.appendTelemetry(batch, builder);

    builder.append("}").append("]");
    return builder.toString();
  }

  public interface JsonCommonBlockWriter {
    <T extends Telemetry> void appendCommonJson(TelemetryBatch<T> batch, StringBuilder builder);
  }

  public interface JsonTelemetryBlockWriter {
    <T extends Telemetry> void appendTelemetry(TelemetryBatch<T> batch, StringBuilder builder);
  }
}
