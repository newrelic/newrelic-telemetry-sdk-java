/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.examples;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.MetricBatchSenderFactory;
import com.newrelic.telemetry.OkHttpPoster;
import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.MetricBatchSender;
import com.newrelic.telemetry.metrics.MetricBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * This example shows an example of generating a Count metric. Count metrics have two requirements:
 *
 * <p>1) They represent the "delta" in the counted value from the previous report.
 *
 * <p>2) They must include the time range over which the delta has accumulated.
 *
 * <p>Additionally, this provides an example of using a {@code
 * com.newrelic.telemetry.metrics.MetricBuffer} to hold on to metrics and send them as a batch.
 *
 * <p>To run this example, provide 2 command line args, the first is the URL to the metric ingest
 * endpoint, and the 2nd is the Insights Insert key.
 */
public class CountExample {

  private static final ThreadLocalRandom random = ThreadLocalRandom.current();

  private static final List<String> items = Arrays.asList("apples", "oranges", "beer", "wine");

  public static void main(String[] args) throws Exception {
    String insightsInsertKey = args[0];

    MetricBatchSenderFactory factory = OkHttpPoster::new;
    MetricBatchSender sender = factory.builder(insightsInsertKey).build();
    MetricBuffer metricBuffer = new MetricBuffer(getCommonAttributes());

    for (int i = 0; i < 10; i++) {
      String item = items.get(random.nextInt(items.size()));
      long startTimeInMillis = System.currentTimeMillis();

      TimeUnit.MILLISECONDS.sleep(5);

      Count purchaseCount = getPurchaseCount(startTimeInMillis, item);
      System.out.println("Recording purchase for: " + item);

      metricBuffer.addMetric(purchaseCount);
    }

    sender.sendBatch(metricBuffer.createBatch());
  }

  /** These attributes are shared across all metrics submitted in the batch. */
  private static Attributes getCommonAttributes() {
    return new Attributes().put("exampleName", "CountExample");
  }

  private static Count getPurchaseCount(long startTimeInMillis, String item) {
    return new Count(
        "purchases",
        random.nextDouble(50, 500),
        startTimeInMillis,
        System.currentTimeMillis(),
        getPurchaseAttributes(item));
  }

  private static Attributes getPurchaseAttributes(String item) {
    Attributes attributes = new Attributes();
    attributes.put("item", item);
    attributes.put("location", "downtown");
    return attributes;
  }
}
