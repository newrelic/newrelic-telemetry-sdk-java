/*
 * ---------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.metrics.json;

import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.metrics.MetricBatch;

public class MetricBatchJsonCommonBlockWriter {

  private final AttributesJson attributesJson;

  public MetricBatchJsonCommonBlockWriter(AttributesJson attributesJson) {
    this.attributesJson = attributesJson;
  }

  public void appendCommonJson(MetricBatch batch, StringBuilder builder) {
    if (batch.hasCommonAttributes()) {
      builder
          .append("\"common\":")
          .append("{")
          .append("\"attributes\":")
          .append(attributesJson.toJson(batch.getCommonAttributes().asMap()))
          .append("}");
    }
  }
}
