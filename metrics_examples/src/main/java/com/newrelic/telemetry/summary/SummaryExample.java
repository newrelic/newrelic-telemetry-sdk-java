/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.summary;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.MetricBatchSender;
import com.newrelic.telemetry.MetricBuffer;
import com.newrelic.telemetry.SimpleMetricBatchSender;
import com.newrelic.telemetry.Summary;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class SummaryExample {

  private static final ThreadLocalRandom random = ThreadLocalRandom.current();

  private static final List<String> endpoints =
      Arrays.asList("/posts (GET)", "/comments (GET)", "/users (GET)");

  public static void main(String[] args) throws MalformedURLException {
    MetricBatchSender sender =
        SimpleMetricBatchSender.builder(args[0])
            .uriOverride(URI.create("https://staging-metric-api.newrelic.com"))
            .build();
    MetricBuffer metricBuffer = new MetricBuffer(getCommonAttributes());

    for (int i = 0; i < 10; i++) {
      String endpoint = endpoints.get(random.nextInt(endpoints.size()));
      long startTimeInMillis = System.currentTimeMillis();

      try {
        Thread.sleep(TimeUnit.SECONDS.toMillis(5)); // 5 seconds between measurements
      } catch (InterruptedException e) {
      }

      Summary throughput = getThroughput(startTimeInMillis, endpoint);
      System.out.println("Recording throughput for: " + endpoint);

      metricBuffer.addMetric(throughput);
    }

    try {
      sender.sendBatch(metricBuffer.createBatch());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Attributes getCommonAttributes() {
    return new Attributes();
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
