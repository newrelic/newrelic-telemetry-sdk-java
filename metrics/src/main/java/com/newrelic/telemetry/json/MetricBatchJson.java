/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */
package com.newrelic.telemetry.json;

public class MetricBatchJson {

  private final AttributesJson attributesJson;
  private final MetricToJson metricToJson;

  public MetricBatchJson(MetricToJson metricToJson, AttributesJson attributesJson) {
    this.attributesJson = attributesJson;
    this.metricToJson = metricToJson;
  }
}
