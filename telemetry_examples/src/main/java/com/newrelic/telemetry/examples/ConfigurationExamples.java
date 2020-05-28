/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.examples;

import com.newrelic.telemetry.EventBatchSenderFactory;
import com.newrelic.telemetry.Java11HttpPoster;
import com.newrelic.telemetry.LogBatchSenderFactory;
import com.newrelic.telemetry.MetricBatchSenderFactory;
import com.newrelic.telemetry.OkHttpPoster;
import com.newrelic.telemetry.SimpleMetricBatchSender;
import com.newrelic.telemetry.SpanBatchSenderFactory;
import com.newrelic.telemetry.TelemetryClient;
import com.newrelic.telemetry.events.EventBatchSender;
import com.newrelic.telemetry.logs.LogBatchSender;
import com.newrelic.telemetry.metrics.MetricBatchSender;
import com.newrelic.telemetry.spans.SpanBatchSender;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.time.Duration;

/** This example shows various ways that you can configure the {@link TelemetryClient}. */
public class ConfigurationExamples {

  public static void main(String[] args) throws MalformedURLException {
    String insightsInsertKey = args[0];

    // Deprecated configuration methods:
    SimpleMetricBatchSender.build(insightsInsertKey);
    MetricBatchSender sender =
        MetricBatchSender.builder()
            .apiKey(insightsInsertKey)
            .httpPoster(new OkHttpPoster(Duration.ofSeconds(2)))
            .build();
    new TelemetryClient(sender, null, null, null);

    // new configuration methods:
    /////////////////////////////////////////////////////////////////////
    // make the simple thing easy:
    //
    // This will configure everything for you, with the given HTTP client implementation, and
    // an API key.
    //
    TelemetryClient client = TelemetryClient.create(Java11HttpPoster::new, insightsInsertKey);

    /////////////////////////////////////////////////////////////////////
    // make the harder thing pretty easy:
    //
    // This is how you can customize your HTTP client implementation.
    //
    TelemetryClient.create(
        () ->
            new Java11HttpPoster(
                HttpClient.newBuilder()
                    // configure custom http configuration here, like proxies, etc.
                    .build()),
        insightsInsertKey);

    /////////////////////////////////////////////////////////////////////
    // make the hardest things possible:
    //
    // If you need full control over all the sending configuration options.
    //
    // Configure your metric sender:
    MetricBatchSender metricBatchSender =
        MetricBatchSender.create(
            MetricBatchSenderFactory.fromHttpImplementation(
                    () ->
                        new Java11HttpPoster(
                            HttpClient.newBuilder()
                                .connectTimeout(Duration.ofSeconds(3))
                                // configure custom stuff here, like proxies, etc.
                                .build()))
                .configureWith(insightsInsertKey)
                .endpoint("http", "special-metrics.com", 80)
                .build());

    // Configure your span sender:
    SpanBatchSender spanBatchSender =
        SpanBatchSender.create(
            SpanBatchSenderFactory.fromHttpImplementation(Java11HttpPoster::new)
                .configureWith(insightsInsertKey)
                .endpoint("https", "special-spans.com", 443)
                .build());

    // Configure your event sender:
    EventBatchSender eventBatchSender =
        EventBatchSender.create(
            EventBatchSenderFactory.fromHttpImplementation(Java11HttpPoster::new)
                .configureWith(insightsInsertKey)
                .endpointWithPath(new URL("http://special-events.com/my-endpoint-rocks/v1/api"))
                .build());

    // Configure your log sender:
    LogBatchSender logBatchSender =
        LogBatchSender.create(
            LogBatchSenderFactory.fromHttpImplementation(Java11HttpPoster::new)
                .configureWith(insightsInsertKey)
                .build());

    // Build your TelemetryClient with the 3 senders.
    new TelemetryClient(metricBatchSender, spanBatchSender, eventBatchSender, logBatchSender);
  }
}
