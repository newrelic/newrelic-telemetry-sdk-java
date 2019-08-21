/*
 * --------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.examples;

import com.newrelic.telemetry.MetricBatchSender;
import com.newrelic.telemetry.MetricBuffer;
import com.newrelic.telemetry.SimpleMetricBatchSender;
import com.newrelic.telemetry.Summary;
import com.newrelic.telemetry.model.Attributes;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * The purpose of this example is to demonstrate sending Summary metrics to New Relic.
 *
 * <p>Summary metrics represent a summarization of some kind of activity over a time window.
 *
 * <p>Additionally, this provides an example of using a {@code com.newrelic.telemetry.MetricBuffer}
 * to hold on to metrics and send them as a batch.
 *
 * <p>To run this example, provide a command line argument for your Insights Insert key.
 */
public class SummaryExample {

  private static final ThreadLocalRandom random = ThreadLocalRandom.current();

  private static final List<String> endpoints =
      Arrays.asList("/posts (GET)", "/comments (GET)", "/users (GET)");

  public static void main(String[] args) throws Exception {
    String insightsInsertKey = args[0];

    MetricBatchSender sender = SimpleMetricBatchSender.builder(insightsInsertKey).build();
    MetricBuffer metricBuffer = new MetricBuffer(getCommonAttributes());

    for (int i = 0; i < 10; i++) {
      String endpoint = endpoints.get(random.nextInt(endpoints.size()));
      long startTimeInMillis = System.currentTimeMillis();

      TimeUnit.SECONDS.sleep(5); // 5 seconds between measurements

      Summary throughput = getThroughput(startTimeInMillis, endpoint);
      System.out.println("Recording throughput for: " + endpoint);

      metricBuffer.addMetric(throughput);
    }

    sender.sendBatch(metricBuffer.createBatch());
  }

  /** These attributes are shared across all metrics submitted in the batch. */
  private static Attributes getCommonAttributes() {
    return new Attributes().put("exampleName", "SummaryExample");
  }

  private static Summary getThroughput(long startTimeInMillis, String endpoint) {
    double min = random.nextDouble(100);
    double max = random.nextDouble(min, 500);
    double sum = random.nextDouble(max, max * 5);

    return new Summary(
        "throughput",
        random.nextInt(1, 5),
        sum,
        min,
        max,
        startTimeInMillis,
        System.currentTimeMillis(),
        getThroughputAttributes(endpoint));
  }

  private static Attributes getThroughputAttributes(String endpoint) {
    Attributes attributes = new Attributes();
    attributes.put("endpoint", endpoint);
    attributes.put("environment", "staging");
    return attributes;
  }
}
