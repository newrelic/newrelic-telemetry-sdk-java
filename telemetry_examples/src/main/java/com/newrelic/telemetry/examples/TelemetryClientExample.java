/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.examples;

import static java.util.Collections.singleton;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.OkHttpPoster;
import com.newrelic.telemetry.TelemetryClient;
import com.newrelic.telemetry.events.Event;
import com.newrelic.telemetry.events.EventBatch;
import com.newrelic.telemetry.logs.Log;
import com.newrelic.telemetry.logs.LogBatch;
import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.MetricBatch;
import com.newrelic.telemetry.metrics.MetricBuffer;
import com.newrelic.telemetry.metrics.Summary;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import java.net.InetAddress;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
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

    // create a TelemetryClient with an http connect timeout of 10 seconds.
    TelemetryClient telemetryClient =
        TelemetryClient.create(
            () -> new OkHttpPoster(Duration.of(10, ChronoUnit.SECONDS)), insightsInsertKey);

    Attributes commonAttributes =
        new Attributes()
            .put("exampleName", "TelemetryClientExample")
            .put("service.name", "TelemetryClientExampleService")
            .put("host.hostname", InetAddress.getLocalHost().getHostName())
            .put("environment", "staging");

    Span span = sendSampleSpan(telemetryClient, commonAttributes);
    sendSampleMetrics(telemetryClient, commonAttributes);
    sendSampleEvent(telemetryClient, commonAttributes);
    sendSampleLogEntry(telemetryClient, commonAttributes, span);

    // make sure to shutdown the client, else the background Executor will stop the program from
    // exiting.
    telemetryClient.shutdown();
  }

  private static void sendSampleLogEntry(
      TelemetryClient telemetryClient, Attributes commonAttributes, Span span) {
    Log log =
        Log.builder()
            .level("INFO")
            .message("Logging a message here about a span.")
            .attributes(
                new Attributes().put("span.id", span.getId()).put("trace.id", span.getTraceId()))
            .build();
    telemetryClient.sendBatch(new LogBatch(singleton(log), commonAttributes));
  }

  private static void sendSampleEvent(
      TelemetryClient telemetryClient, Attributes commonAttributes) {
    Event event = new Event("TestEvent", new Attributes().put("testKey", "testValue"));
    telemetryClient.sendBatch(new EventBatch(singleton(event), commonAttributes));
  }

  private static void sendSampleMetrics(
      TelemetryClient telemetryClient, Attributes commonAttributes) {
    long startTime = System.currentTimeMillis();

    MetricBuffer metricBuffer =
        MetricBuffer.builder()
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

  private static Span sendSampleSpan(TelemetryClient telemetryClient, Attributes commonAttributes) {
    Span sampleSpan =
        Span.builder(UUID.randomUUID().toString())
            .timestamp(System.currentTimeMillis())
            .traceId(UUID.randomUUID().toString())
            .durationMs(150d)
            .name("testSpan")
            .build();
    String traceId = UUID.randomUUID().toString();
    SpanBatch spanBatch = new SpanBatch(singleton(sampleSpan), commonAttributes, traceId);
    telemetryClient.sendBatch(spanBatch);
    return sampleSpan;
  }
}
