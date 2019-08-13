/*
 * --------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.examples;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Gauge;
import com.newrelic.telemetry.MetricBatchSender;
import com.newrelic.telemetry.MetricBuffer;
import com.newrelic.telemetry.SimpleMetricBatchSender;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * The purpose of this example is to demonstrate sending Gauge metrics to New Relic.
 *
 * <p>A gauge represents a numeric value measured at a point in time.
 *
 * <p>Additionally, this provides an example of using a {@code com.newrelic.telemetry.MetricBuffer}
 * to hold on to metrics and send them as a batch.
 *
 * <p>To run this example, provide 2 command line args, the first is the URL to the metric ingest *
 * endpoint, and the 2nd is the Insights Insert key.
 */
public class GaugeExample {

  private static final ThreadLocalRandom random = ThreadLocalRandom.current();

  private static final List<String> rooms =
      Arrays.asList("bedroom", "dining_room", "living_room", "basement");

  public static void main(String[] args) throws Exception {
    URI metricApiEndpoint = URI.create(args[0]);
    String insightsInsertKey = args[1];

    MetricBatchSender sender =
        SimpleMetricBatchSender.builder(insightsInsertKey).uriOverride(metricApiEndpoint).build();
    MetricBuffer metricBuffer = new MetricBuffer(getCommonAttributes());

    for (int i = 0; i < 10; i++) {
      Gauge currentTemperature = getCurrentTemperature();
      System.out.println("Recording temperature: " + currentTemperature);

      metricBuffer.addMetric(currentTemperature);

      TimeUnit.SECONDS.sleep(5); // 5 seconds between measurements
    }

    sender.sendBatch(metricBuffer.createBatch());
  }

  /** These attributes are shared across all metrics submitted in the batch. */
  private static Attributes getCommonAttributes() {
    return new Attributes().put("exampleName", "GaugeExample");
  }

  private static Gauge getCurrentTemperature() {
    return new Gauge(
        "temperature",
        random.nextDouble(60, 90),
        System.currentTimeMillis(),
        getTemperatureAttributes());
  }

  private static Attributes getTemperatureAttributes() {
    Attributes attributes = new Attributes();
    attributes.put("room", rooms.get(random.nextInt(rooms.size())));
    attributes.put("occupied", random.nextBoolean());
    attributes.put("humidity", random.nextInt(40, 60));
    return attributes;
  }
}
