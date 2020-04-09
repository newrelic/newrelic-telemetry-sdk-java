/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.events.json;

import com.newrelic.telemetry.events.EventBatch;
import com.newrelic.telemetry.json.AttributesJson;

public class EventBatchJsonCommonBlockWriter {

  private final AttributesJson attributesJson;

  public EventBatchJsonCommonBlockWriter(AttributesJson attributesJson) {
    this.attributesJson = attributesJson;
  }

  public void appendCommonJson(EventBatch batch, StringBuilder builder) {
    //    if (batch.hasCommonAttributes()) {
    //      builder
    //          .append("\"common\":")
    //          .append("{")
    //          .append("\"attributes\":")
    //          .append(attributesJson.toJson(batch.getCommonAttributes().asMap()))
    //          .append("}");
    //    }
  }
}
