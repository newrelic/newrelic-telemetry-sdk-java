/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.gauge;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Gauge;
import com.newrelic.telemetry.MetricBatchSender;
import com.newrelic.telemetry.MetricBuffer;
import com.newrelic.telemetry.SimpleMetricBatchSender;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class GaugeExample {

  private static final ThreadLocalRandom random = ThreadLocalRandom.current();

  private static final List<String> rooms =
      Arrays.asList("bedroom", "dining_room", "living_room", "basement");

  public static void main(String[] args) throws MalformedURLException {
    MetricBatchSender sender =
        SimpleMetricBatchSender.builder(args[0])
            .uriOverride(URI.create("https://staging-metric-api.newrelic.com"))
            .build();
    MetricBuffer metricBuffer = new MetricBuffer(getCommonAttributes());

    for (int i = 0; i < 10; i++) {
      Gauge currentTemperature = getCurrentTemperature();
      System.out.println("Recording temperature: " + currentTemperature);

      metricBuffer.addMetric(currentTemperature);

      try {
        Thread.sleep(TimeUnit.SECONDS.toMillis(5)); // 5 seconds between measurements
      } catch (InterruptedException e) {
      }
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
