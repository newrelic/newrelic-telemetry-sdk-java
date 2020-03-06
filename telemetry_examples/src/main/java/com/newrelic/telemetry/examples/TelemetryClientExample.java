/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.examples;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.SimpleMetricBatchSender;
import com.newrelic.telemetry.SimpleSpanBatchSender;
import com.newrelic.telemetry.TelemetryClient;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.metrics.*;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
import java.net.InetAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.UUID;

/**
 * This example shows how to use the TelemetryClient to handle standard error conditions.
 *
 * <p>It also demonstrates that a single MetricBatch can contain metrics of different types.
 *
 * <p>To run this example, provide a command line argument for your Insights Insert key.
 */
public class TelemetryClientExample {

  public static void main(String[] args) throws Exception {
    String insightsInsertKey = args[0];

    MetricBatchSender batchSender =
        SimpleMetricBatchSender.builder(insightsInsertKey, Duration.of(10, ChronoUnit.SECONDS))
            .build();
    SpanBatchSender spanBatchSender = SimpleSpanBatchSender.builder(insightsInsertKey).build();

    // FIXME Need example with events
    TelemetryClient telemetryClient = new TelemetryClient(spanBatchSender, batchSender, null);

    Attributes commonAttributes = new Attributes().put("exampleName", "TelemetryClientExample");
    commonAttributes.put("host", InetAddress.getLocalHost().getHostName());
    commonAttributes.put("appName", "testApplication");
    commonAttributes.put("environment", "staging");

    sendSampleSpan(telemetryClient, commonAttributes);
    sendSampleMetrics(telemetryClient, commonAttributes);

    // make sure to shutdown the client, else the background Executor will stop the program from
    // exiting.
    telemetryClient.shutdown();
  }

  private static void sendSampleMetrics(
      TelemetryClient telemetryClient, Attributes commonAttributes) {
    long startTime = System.currentTimeMillis();

    MetricBuffer metricBuffer =
        MetricBuffer.builder()
            .serviceName("Sample Service")
            .instrumentationProvider("Manual instrumentation")
            .attributes(commonAttributes)
            .build();
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

    // The TelemetryClient uses the recommended techniques for responding to errors from the
    // New Relic APIs. It uses a background thread to schedule the sending, handling retries
    // transparently.
    telemetryClient.sendBatch(batch);
  }

  private static void sendSampleSpan(TelemetryClient telemetryClient, Attributes commonAttributes)
      throws ResponseException {
    Span sampleSpan =
        Span.builder(UUID.randomUUID().toString())
            .timestamp(System.currentTimeMillis())
            .durationMs(150d)
            .serviceName("Test Service")
            .name("testSpan")
            .build();
    String traceId = UUID.randomUUID().toString();
    SpanBatch spanBatch =
        new SpanBatch(Collections.singleton(sampleSpan), commonAttributes, traceId);
    telemetryClient.sendBatch(spanBatch);
  }
}
