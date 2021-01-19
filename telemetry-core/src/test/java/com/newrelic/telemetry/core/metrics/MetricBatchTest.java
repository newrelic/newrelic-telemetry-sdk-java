/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core.metrics;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.common.collect.ImmutableMap;
import com.newrelic.telemetry.core.Attributes;
import com.newrelic.telemetry.core.TelemetryBatch;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
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

    assertTrue(splitBatches.contains(new MetricBatch(singletonList(metric1), commonAttributes)));
    assertTrue(splitBatches.contains(new MetricBatch(singletonList(metric2), commonAttributes)));
  }

  @Test
  @DisplayName("A Metric Batch with a single element can be split")
  void testSplitSingle() throws Exception {
    Attributes commonAttributes = new Attributes().put("a", 5).put("b", 99);

    DummyMetric metric1 = new DummyMetric("foo", ImmutableMap.of("One", "two"));
    MetricBatch testBatch = new MetricBatch(Arrays.asList(metric1), commonAttributes);

    List<TelemetryBatch<Metric>> splitBatches = testBatch.split();
    assertEquals(2, splitBatches.size());

    assertTrue(splitBatches.contains(new MetricBatch(singletonList(metric1), commonAttributes)));
    assertTrue(splitBatches.contains(new MetricBatch(emptyList(), commonAttributes)));
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
    assertTrue(splitBatches.contains(new MetricBatch(singletonList(metric1), commonAttributes)));
    assertTrue(
        splitBatches.contains(new MetricBatch(Arrays.asList(metric2, metric3), commonAttributes)));
  }

  @Test
  @DisplayName("An empty Metric Batch can be split, resulting in no batches")
  void testSplitEmpty() throws Exception {
    Attributes commonAttributes = new Attributes().put("a", 5).put("b", 99);

    MetricBatch testBatch = new MetricBatch(emptyList(), commonAttributes);

    List<TelemetryBatch<Metric>> splitBatches = testBatch.split();
    assertEquals(0, splitBatches.size());
  }

  @Test
  void testBuilder() throws Exception {
    String serviceName = "foxtrot";
    String provider = "super-duper-instrumentation";
    Attributes attributes = new Attributes().put("a", "b");
    Metric metric1 = new Count("foo", 12, 123, 124, new Attributes());
    Metric metric2 = new Count("bar", 13, 123, 124, new Attributes());
    Collection<Metric> metrics = Arrays.asList(metric1, metric2);
    Attributes expectedAttributes =
        new Attributes(attributes)
            .put("service.name", serviceName)
            .put("instrumentation.provider", provider);
    MetricBatch result =
        MetricBatch.builder()
            .attributes(attributes)
            .metrics(metrics)
            .serviceName(serviceName)
            .instrumentationProvider(provider)
            .build();
    assertEquals(metrics, result.getTelemetry());
    assertEquals(expectedAttributes, result.getCommonAttributes());
  }

  private static class DummyMetric implements Metric {
    private final String name;
    private final Map<String, Object> attributes;

    private DummyMetric(String name, Map<String, Object> attributes) {
      this.name = name;
      this.attributes = attributes;
    }
  }
}
