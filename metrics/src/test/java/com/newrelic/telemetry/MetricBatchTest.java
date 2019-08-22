/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import static com.newrelic.telemetry.Telemetry.Type.METRIC;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Value;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MetricBatchTest {

  @Test
  @DisplayName("A Metric Batch with an even number of elements can be split into 2 pieces")
  void testSplitEven() throws Exception {
    Attributes commonAttributes = new Attributes().put("a", 5).put("b", 99);

    DummyMetric metric1 = new DummyMetric("foo", ImmutableMap.of("One", "two"));
    DummyMetric metric2 = new DummyMetric("bar", ImmutableMap.of("Two", "one"));
    MetricBatch testBatch = new MetricBatch(Arrays.asList(metric1, metric2), commonAttributes);

    List<TelemetryBatch<Metric>> splitBatches = testBatch.split();
    assertEquals(2, splitBatches.size());

    assertTrue(
        splitBatches.contains(
            new TelemetryBatch<Metric>(METRIC, singletonList(metric1), commonAttributes)));
    assertTrue(
        splitBatches.contains(
            new TelemetryBatch<Metric>(METRIC, singletonList(metric2), commonAttributes)));
  }

  @Test
  @DisplayName("A Metric Batch with a single element can be split")
  void testSplitSingle() throws Exception {
    Attributes commonAttributes = new Attributes().put("a", 5).put("b", 99);

    DummyMetric metric1 = new DummyMetric("foo", ImmutableMap.of("One", "two"));
    MetricBatch testBatch = new MetricBatch(Arrays.asList(metric1), commonAttributes);

    List<TelemetryBatch<Metric>> splitBatches = testBatch.split();
    assertEquals(2, splitBatches.size());

    assertTrue(
        splitBatches.contains(
            new TelemetryBatch<Metric>(METRIC, singletonList(metric1), commonAttributes)));
    assertTrue(
        splitBatches.contains(new TelemetryBatch<Metric>(METRIC, emptyList(), commonAttributes)));
  }

  @Test
  @DisplayName("A Metric Batch with an odd number of elements can be split into 2 pieces")
  void testSplitOdd() throws Exception {
    Attributes commonAttributes = new Attributes().put("a", 5).put("b", 99);

    DummyMetric metric1 = new DummyMetric("foo", ImmutableMap.of("One", "two"));
    DummyMetric metric2 = new DummyMetric("bar", ImmutableMap.of("Two", "one"));
    DummyMetric metric3 = new DummyMetric("baz", ImmutableMap.of("Three", "four"));
    MetricBatch testBatch =
        new MetricBatch(Arrays.asList(metric1, metric2, metric3), commonAttributes);

    List<TelemetryBatch<Metric>> splitBatches = testBatch.split();
    assertEquals(2, splitBatches.size());
    assertTrue(
        splitBatches.contains(
            new TelemetryBatch<Metric>(METRIC, singletonList(metric1), commonAttributes)));
    assertTrue(
        splitBatches.contains(
            new TelemetryBatch<Metric>(METRIC, Arrays.asList(metric2, metric3), commonAttributes)));
  }

  @Test
  @DisplayName("An empty Metric Batch can be split, resulting in no batches")
  void testSplitEmpty() throws Exception {
    Attributes commonAttributes = new Attributes().put("a", 5).put("b", 99);

    MetricBatch testBatch = new MetricBatch(emptyList(), commonAttributes);

    List<TelemetryBatch<Metric>> splitBatches = testBatch.split();
    assertEquals(0, splitBatches.size());
  }

  @Value
  private static class DummyMetric implements Metric {

    String name;
    Map<String, Object> attributes;
  }
}
