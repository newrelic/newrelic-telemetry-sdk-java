/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.Telemetry;

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
public interface Metric extends Telemetry {}
