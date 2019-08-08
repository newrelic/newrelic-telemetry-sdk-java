/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.exceptions.RetryWithBackoffException;
import com.newrelic.telemetry.exceptions.RetryWithRequestedWaitException;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class Examples {

  public static void main(String[] args) throws MalformedURLException, UnknownHostException {
    MetricBatchSender sender =
        SimpleMetricBatchSender.builder(
                "TOPSECRETAPIKEYGOESHERE", Duration.of(10, ChronoUnit.SECONDS))
            .uriOverride(URI.create("https://staging-metric-api.newrelic.com"))
            .build();

    Attributes commonAttributes = new Attributes();
    commonAttributes.put("host", InetAddress.getLocalHost().getHostName());
    commonAttributes.put("appName", "testApplication");
    commonAttributes.put("environment", "staging");

    long startTime = System.currentTimeMillis();

    MetricBuffer metricBuffer = new MetricBuffer(commonAttributes);
    metricBuffer.addMetric(
        new Gauge("temperatureC", 44d, startTime, new Attributes().put("room", "kitchen")));
    metricBuffer.addMetric(
        new Gauge("temperatureC", 25d, startTime, new Attributes().put("room", "bathroom")));
    metricBuffer.addMetric(
        new Gauge("temperatureC", 10d, startTime, new Attributes().put("room", "basement")));

    metricBuffer.addMetric(
        new Count(
            "bugsSquashed",
            5d,
            startTime,
            System.currentTimeMillis(),
            new Attributes().put("project", "JAVA")));

    metricBuffer.addMetric(
        new Summary(
            "throughput", 25, 100, 1, 10, startTime, System.currentTimeMillis(), new Attributes()));

    MetricBatch batch = metricBuffer.createBatch();

    try {
      sender.sendBatch(batch);
    } catch (RetryWithBackoffException e) {
      // wait & retry later
    } catch (RetryWithRequestedWaitException e) {
      int waitTime = e.getWaitTime();
      // wait and retry after wait time
    } catch (RetryWithSplitException e) {
      List<MetricBatch> smallerBatches = batch.split();
      for (MetricBatch smallerBatch : smallerBatches) {
        // send the sub-batch here
      }
    } catch (ResponseException e) {
      // log and give up!
    }
  }
}
