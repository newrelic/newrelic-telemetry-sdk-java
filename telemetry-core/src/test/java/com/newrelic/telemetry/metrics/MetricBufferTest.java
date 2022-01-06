/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.newrelic.telemetry.Attributes;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MetricBufferTest {

  @Test
  @DisplayName("Common attributes are stored and returned correctly")
  void testCommonAttributes() {
    Attributes expectedCommonAttributes =
        new Attributes().put("key1", "value1").put("key2", true).put("key3", 123.456);

    MetricBuffer metricBuffer = new MetricBuffer(expectedCommonAttributes);

    assertEquals(expectedCommonAttributes, metricBuffer.getCommonAttributes());
  }

  @Test
  @DisplayName("Metrics are stored and returned correctly")
  void testMetrics() {
    Attributes expectedCommonAttributes =
        new Attributes().put("key1", "value1").put("key2", true).put("key3", 123.456);

    MetricBuffer metricBuffer = new MetricBuffer(expectedCommonAttributes);
    Gauge expectedGauge =
        new Gauge("testGauge", 100.00, System.currentTimeMillis(), new Attributes());
    metricBuffer.addMetric(expectedGauge);

    assertEquals(1, metricBuffer.getMetrics().size());
    assertEquals(expectedGauge, metricBuffer.getMetrics().poll());
  }

  @Test
  @DisplayName("Get size of Metrics")
  void testMetricsSize() {
    Attributes expectedCommonAttributes =
        new Attributes().put("key1", "value1").put("key2", true).put("key3", 123.456);

    MetricBuffer metricBuffer = new MetricBuffer(expectedCommonAttributes);
    Gauge expectedGauge =
        new Gauge("testGauge", 100.00, System.currentTimeMillis(), new Attributes());
    assertEquals(0, metricBuffer.size());
    metricBuffer.addMetric(expectedGauge);
    assertEquals(1, metricBuffer.size());
  }

  @Test
  @DisplayName("Null keys & values don't break things")
  void testNullValues() {
    Attributes commonAttributes = new Attributes();
    commonAttributes.put("null", (String) null);
    commonAttributes.put(null, "nullKey");

    MetricBuffer metricBuffer = new MetricBuffer(commonAttributes);
    metricBuffer.addMetric(new Gauge("foo", 4.4d, 3999L, commonAttributes));
    metricBuffer.addMetric(new Summary("bar", 3, 44d, 22d, 99d, 0, 10, commonAttributes));
    metricBuffer.addMetric(new Count("baz", 44f, 0, 10, commonAttributes));

    MetricBatch batch = metricBuffer.createBatch();

    assertEquals(commonAttributes, batch.getCommonAttributes());
  }

  @Test
  @DisplayName("Verify that the builder sets up common attributes")
  void testBuilder() {
    String serviceName = "gopher";
    String provider = "sweet_instrumentation";
    Attributes attributes = new Attributes().put("foo", "bar");
    Attributes expectedAttributes =
        new Attributes(attributes)
            .put("service.name", serviceName)
            .put("instrumentation.provider", provider);

    MetricBuffer buffer =
        MetricBuffer.builder()
            .attributes(attributes)
            .serviceName(serviceName)
            .instrumentationProvider(provider)
            .build();
    assertEquals(expectedAttributes, buffer.getCommonAttributes());
  }
}
