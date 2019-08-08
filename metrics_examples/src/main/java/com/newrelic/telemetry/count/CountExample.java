/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.count;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Count;
import com.newrelic.telemetry.MetricBatchSender;
import com.newrelic.telemetry.MetricBuffer;
import com.newrelic.telemetry.SimpleMetricBatchSender;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class CountExample {

  private static final ThreadLocalRandom random = ThreadLocalRandom.current();

  private static final List<String> items = Arrays.asList("apples", "oranges", "beer", "wine");

  public static void main(String[] args) throws MalformedURLException {
    MetricBatchSender sender =
        SimpleMetricBatchSender.builder(args[0])
            .uriOverride(URI.create("https://staging-metric-api.newrelic.com"))
            .build();
    MetricBuffer metricBuffer = new MetricBuffer(getCommonAttributes());

    for (int i = 0; i < 10; i++) {
      String item = items.get(random.nextInt(items.size()));
      long startTimeInMillis = System.currentTimeMillis();

      try {
        Thread.sleep(TimeUnit.SECONDS.toMillis(5)); // 5 seconds between measurements
      } catch (InterruptedException e) {
      }

      Count purchaseCount = getPurchaseCount(startTimeInMillis, item);
      System.out.println("Recording purchase for: " + item);

      metricBuffer.addMetric(purchaseCount);
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
