/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.newrelic.telemetry.Attributes;
import java.util.ArrayList;
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

    ArrayList<MetricBatch> batches = metricBuffer.createBatch();

    assertEquals(commonAttributes, batches.get(0).getCommonAttributes());
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

  @Test
  @DisplayName("Check that one batch is created")
  void testCreateOneBatch() {

    long startTimeMillis = 300;
    long currentTimeMillis = 350;
    double testDouble = 60.3;

    MetricBuffer testMetricBuffer = new MetricBuffer(new Attributes());

    Attributes testAttributes = new Attributes();
    testAttributes.put("item", "Apple");
    testAttributes.put("location", "downtown");

    Count testCount =
        new Count("TestEvent", testDouble, startTimeMillis, currentTimeMillis, testAttributes);
    testMetricBuffer.addMetric(testCount);

    ArrayList<MetricBatch> testBatchesList = testMetricBuffer.createBatch();
    assertEquals(1, testBatchesList.size());
  }

  @Test
  @DisplayName("Multiple Metric Batches Not Enabled: Check if a single batch is created")
  void testCreateSingleBatchWithMultipleNotEnabled() {
    /**
     * The uncompressed payload size for this example is 268000070 bytes. If splitOnSizeLimit =
     * true, then 2 batches should be created. This is because the maximum uncompressed payload size
     * for a batch is 180000000 bytes. However, since splitOnSizeLimit = false (by default), only 1
     * batch should be created.
     */
    long startTimeMillis = 300;
    long currentTimeMillis = 350;
    double testDouble = 60.3;

    Attributes testAttributes = new Attributes();
    testAttributes.put("item", "Apple");
    testAttributes.put("location", "downtown");

    MetricBuffer testMetricBuffer = new MetricBuffer(new Attributes());

    for (int i = 0; i < 1500000; i++) {
      Count testCount =
          new Count("TestEvent", testDouble, startTimeMillis, currentTimeMillis, testAttributes);
      testMetricBuffer.addMetric(testCount);
    }

    ArrayList<MetricBatch> testEventBatches = testMetricBuffer.createBatch();
    assertEquals(1, testEventBatches.size());
  }

  @Test
  @DisplayName("Multiple Metric Batches Enabled: Check if a single batch is created")
  void testCreateOneBatchWithMultipleEnabled() {

    long startTimeMillis = 300;
    long currentTimeMillis = 350;
    double testDouble = 60.3;

    MetricBuffer testMetricBuffer = new MetricBuffer(new Attributes(), true);

    Attributes testAttributes = new Attributes();
    testAttributes.put("item", "Apple");
    testAttributes.put("location", "downtown");

    Count testCount =
        new Count("TestEvent", testDouble, startTimeMillis, currentTimeMillis, testAttributes);
    testMetricBuffer.addMetric(testCount);

    ArrayList<MetricBatch> testBatchesList = testMetricBuffer.createBatch();
    assertEquals(1, testBatchesList.size());
  }

  @Test
  @DisplayName("Multiple Metric Batches Enabled: Check that multiple batches are created")
  void testCreateMultipleBatchesWithMultipleEnabled() {

    /**
     * The uncompressed payload size for this example is 268000070 bytes (from the .createBatch()
     * method). Since the maximum uncompressed payload size for a batch is 180000000 bytes, 2
     * batches should be created.
     */
    long startTimeMillis = 300;
    long currentTimeMillis = 350;
    double testDouble = 60.3;

    Attributes testAttributes = new Attributes();
    testAttributes.put("item", "Apple");
    testAttributes.put("location", "downtown");

    MetricBuffer testMetricBuffer = new MetricBuffer(new Attributes(), true);

    for (int i = 0; i < 1500000; i++) {
      Count testCount =
          new Count("TestEvent", testDouble, startTimeMillis, currentTimeMillis, testAttributes);
      testMetricBuffer.addMetric(testCount);
    }

    ArrayList<MetricBatch> testBatchesList = testMetricBuffer.createBatch();
    assertEquals(2, testBatchesList.size());
  }
}
