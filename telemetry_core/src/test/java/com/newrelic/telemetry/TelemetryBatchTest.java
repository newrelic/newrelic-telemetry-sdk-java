/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.Metric;
import com.newrelic.telemetry.metrics.MetricBatch;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class TelemetryBatchTest {

  @Test
  void testEmpty() {
    TelemetryBatch<Metric> batch1 = new MetricBatch(emptyList(), new Attributes());
    Metric metric = new Count("a", 12.0, 123, 456, new Attributes());
    TelemetryBatch<Metric> batch2 =
        new MetricBatch(Collections.singleton(metric), new Attributes());
    assertTrue(batch1.isEmpty());
    assertFalse(batch2.isEmpty());
  }

  @Test
  void testHasUUID() {
    TelemetryBatch<Metric> batch1 = new MetricBatch(emptyList(), new Attributes());
    TelemetryBatch<Metric> batch2 = new MetricBatch(emptyList(), new Attributes());
    assertNotEquals(batch1.getUuid(), batch2.getUuid());
  }

  @Test
  void testUuidFormat() {
    TelemetryBatch<Metric> batch = new MetricBatch(emptyList(), new Attributes());
    //2280a03c-9c2a-4527-ac32-1332e8de159c
    String regex = "^[0-9a-z]{8}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{4}-[0-9a-z]{12}$";
    assertTrue(batch.getUuid().toString().matches(regex));
  }
}
