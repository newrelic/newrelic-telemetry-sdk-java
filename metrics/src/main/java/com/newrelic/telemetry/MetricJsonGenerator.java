/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * If you wish to provide your own json implementation, an implementation of this interface must be
 * provided. All of the methods must return valid JSON with properly escaped strings.
 *
 * <p>TODO: add links to external documentation of the json format and APIs.
 */
public interface MetricJsonGenerator {
  Logger logger = LoggerFactory.getLogger(MetricJsonGenerator.class);

  /**
   * Summary json must be structured this to be accepted by the New Relic backend.
   *
   * <p>the timestamp should be in ms since Unix epoch
   *
   * <p>The attributes are optional, if not present
   *
   * <pre>
   * {
   *   "name": "mySummaryName",
   *   "type": "summary",
   *   "value: {
   *     "count": 33,
   *     "sum": 1234,
   *     "min": 10,
   *     "max": 80
   *   },
   *   "timestamp": 1560807066910,
   *   "interval.ms": 5000,
   *   "attributes": {
   *     "key1": "value1",
   *     "key2", 555,
   *     "key3", false
   *   }
   * }
   * </pre>
   */
  String writeSummaryJson(Summary summary);

  /**
   * Gauge json must be structured this to be accepted by the New Relic backend.
   *
   * <p>the timestamp should be in ms since Unix epoch
   *
   * <p>The attributes are optional, if not present
   *
   * <pre>
   * {
   *   "name": "myGaugeName",
   *   "type": "gauge",
   *   "value: 556.888,
   *   "timestamp": 1560807066910,
   *   "attributes": {
   *     "key1": "value1",
   *     "key2", 555,
   *     "key3", false
   *   }
   * }
   * </pre>
   */
  String writeGaugeJson(Gauge gauge);

  /**
   * Count json must be structured this to be accepted by the New Relic backend.
   *
   * <p>the timestamp should be in ms since Unix epoch
   *
   * <p>The attributes are optional, if not present
   *
   * <pre>
   * {
   *   "name": "myGaugeName",
   *   "type": "count",
   *   "value: 556.888,
   *   "timestamp": 1560807066910,
   *   "interval.ms": 5000,
   *   "attributes": {
   *     "key1": "value1",
   *     "key2", 555,
   *     "key3", false
   *   }
   * }
   * </pre>
   */
  String writeCountJson(Count count);
}
