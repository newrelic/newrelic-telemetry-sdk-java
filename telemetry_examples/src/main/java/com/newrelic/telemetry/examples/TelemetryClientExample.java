/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.examples;

import static java.util.Collections.singleton;

import com.newrelic.telemetry.*;
import com.newrelic.telemetry.events.Event;
import com.newrelic.telemetry.events.EventBatch;
import com.newrelic.telemetry.events.EventBatchSender;
import com.newrelic.telemetry.metrics.*;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
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

    MetricBatchSenderFactory metricFactory = MetricBatchSenderFactory.ofSender(OkHttpPoster::new);
    MetricBatchSender metricBatchSender =
        metricFactory.builder(insightsInsertKey, Duration.of(10, ChronoUnit.SECONDS)).build();

    SpanBatchSenderFactory spanFactory = SpanBatchSenderFactory.ofSender(OkHttpPoster::new);
    SpanBatchSender spanBatchSender = spanFactory.builder(insightsInsertKey).build();

    // a fully customized example
    EventBatchSenderFactory eventBatchSenderFactory =
        EventBatchSenderFactory.withHttpImplementation(OkHttpPoster::new);
    SenderConfiguration senderConfiguration =
        eventBatchSenderFactory.configureWith(insightsInsertKey).auditLoggingEnabled(true).build();

    // the simplest possible application
    EventBatchSender simplestSender = eventBatchSenderFactory.createBatchSender(insightsInsertKey);

    EventBatchSender eventBatchSender = EventBatchSender.create(senderConfiguration);

    TelemetryClient telemetryClient =
        new TelemetryClient(metricBatchSender, spanBatchSender, eventBatchSender);

    Attributes commonAttributes = new Attributes().put("exampleName", "TelemetryClientExample");
    commonAttributes.put("host.hostname", InetAddress.getLocalHost().getHostName());
    commonAttributes.put("environment", "staging");

    sendSampleSpan(telemetryClient, commonAttributes);
    sendSampleMetrics(telemetryClient, commonAttributes);
    sendSampleEvent(telemetryClient, commonAttributes);

    // make sure to shutdown the client, else the background Executor will stop the program from
    // exiting.
    telemetryClient.shutdown();
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

  private static void sendSampleSpan(TelemetryClient telemetryClient, Attributes commonAttributes) {
    Span sampleSpan =
        Span.builder(UUID.randomUUID().toString())
            .timestamp(System.currentTimeMillis())
            .durationMs(150d)
            .serviceName("Test Service")
            .name("testSpan")
            .build();
    String traceId = UUID.randomUUID().toString();
    SpanBatch spanBatch = new SpanBatch(singleton(sampleSpan), commonAttributes, traceId);
    telemetryClient.sendBatch(spanBatch);
  }
}
