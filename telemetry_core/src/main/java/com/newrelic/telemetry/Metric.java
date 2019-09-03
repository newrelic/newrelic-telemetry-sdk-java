/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

/**
 * A Metric is a very low-level data structure, recorded in the New Relic Metric API.
 *
 * <p>All Metric instances allow you to specify an arbitrary set of attributes, which can be used
 * for faceted querying in New Relic.
 *
 * <p>This is a tag-interface and contains no inherent functionality.
 *
 * @see Gauge
 * @see Count
 * @see Summary
 */
public interface Metric extends Telemetry {

  @Override
  default Type getType() {
    return Type.METRIC;
  };
}
