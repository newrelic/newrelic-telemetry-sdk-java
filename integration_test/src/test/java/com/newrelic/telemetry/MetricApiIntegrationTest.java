/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.model.JsonBody.json;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;
import com.newrelic.telemetry.exceptions.DiscardBatchException;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.exceptions.RetryWithBackoffException;
import com.newrelic.telemetry.exceptions.RetryWithRequestedWaitException;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.MetricBatchSender;
import com.newrelic.telemetry.metrics.MetricBuffer;
import com.newrelic.telemetry.metrics.Summary;
import java.net.URI;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.Delay;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class MetricApiIntegrationTest {

  private static final int SERVICE_PORT = 1080 + new Random().nextInt(900);
  private static String containerIpAddress;
  private static MockServerClient mockServerClient;

  private static final GenericContainer<?> container =
      new GenericContainer<>("jamesdbloom/mockserver:mockserver-5.5.1")
          .withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()))
          .withExposedPorts(SERVICE_PORT);

  private MetricBatchSender metricBatchSender;

  @BeforeAll
  static void beforeClass() {
    container.setPortBindings(singletonList(SERVICE_PORT + ":1080"));
    container.setWaitStrategy(new WaitAllStrategy());
    container.setStartupCheckStrategy(
        new MinimumDurationRunningStartupCheckStrategy(Duration.of(10, SECONDS)));
    container.start();
    containerIpAddress = container.getContainerIpAddress();
    mockServerClient = new MockServerClient(containerIpAddress, SERVICE_PORT);
  }

  @BeforeEach
  void setUp() throws Exception {
    mockServerClient.reset();
    MetricBatchSenderFactory factory =
        MetricBatchSenderFactory.fromHttpImplementation(OkHttpPoster::new);
    SenderConfiguration config =
        factory
            .configureWith("fakeKey", Duration.ofMillis(1500))
            .endpointUrl(URI.create("http://" + containerIpAddress + ":" + SERVICE_PORT).toURL())
            .secondaryUserAgent("testApplication/1.0.0")
            .build();
    metricBatchSender = MetricBatchSender.create(config);
  }

  @Test
  @DisplayName("Low Level SDK can send a metric batch and returns a successful response")
  void testSuccessfulMetricSend() throws Exception {
    MetricPayload expectedPayload =
        new MetricPayload(
            singletonMap("attributes", singletonMap("key1", "val1")),
            Arrays.asList(
                ImmutableMap.<String, Object>builder()
                    .put("name", "myCounter")
                    .put("type", "count")
                    .put("value", 1.0d)
                    .put("timestamp", 350)
                    .put("interval.ms", 42)
                    .put("attributes", singletonMap("key2", "val2"))
                    .build(),
                ImmutableMap.<String, Object>builder()
                    .put("name", "mySummary")
                    .put("type", "summary")
                    .put("value", ImmutableMap.of("count", 5, "sum", 33.5, "min", 1.0, "max", 10.0))
                    .put("timestamp", 1111111)
                    .put("interval.ms", 1111111)
                    .put("attributes", singletonMap("key3", "val3"))
                    .build(),
                ImmutableMap.<String, Object>builder()
                    .put("name", "myGauge")
                    .put("type", "gauge")
                    .put("value", 22.554d)
                    .put("timestamp", 4444444)
                    .put("attributes", singletonMap("key4", "val4"))
                    .build()));
    mockServerClient
        .when(
            new HttpRequest()
                .withMethod("POST")
                .withPath("/metric/v1")
                .withBody(
                    json(
                        new MetricPayload[] {expectedPayload},
                        MediaType.JSON_UTF_8,
                        MatchType.STRICT))
                .withHeader("User-Agent", "NewRelic-Java-TelemetrySDK/.* testApplication/1.0.0")
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withHeader("Content-Length", ".*"))
        .respond(new HttpResponse().withStatusCode(202));

    Attributes countAttributes = new Attributes().put("key2", "val2");
    Attributes summaryAttributes = new Attributes().put("key3", "val3");
    Attributes gaugeAttributes = new Attributes().put("key4", "val4");

    long currentTimeMillis = 350;

    MetricBuffer metricBuffer = new MetricBuffer(new Attributes().put("key1", "val1"));
    metricBuffer.addMetric(
        new Count("myCounter", 1, currentTimeMillis, currentTimeMillis + 42, countAttributes));
    metricBuffer.addMetric(
        new Summary("mySummary", 5, 33.5d, 1.0d, 10d, 1111111, 2222222, summaryAttributes));
    metricBuffer.addMetric(new Gauge("myGauge", 22.554d, 4444444, gaugeAttributes));
    Response response = metricBatchSender.sendBatch(metricBuffer.createBatch());

    assertEquals(202, response.getStatusCode());
    assertEquals("Accepted", response.getStatusMessage());
  }

  @Test
  @DisplayName("SDK responds with RetryWithBackoffException to MetricBatchSender timeout")
  void testMetricBatchSenderTimeoutExceptionResponse() throws Exception {
    mockServerClient
        .when(
            new HttpRequest()
                .withMethod("POST")
                .withPath("/metric/v1")
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withHeader("Content-Length", ".*"))
        .respond(new HttpResponse().withStatusCode(202).withDelay(new Delay(TimeUnit.SECONDS, 2)));

    Attributes countAttributes = new Attributes().put("key2", "val2");

    long currentTimeMillis = 350;

    MetricBuffer metricBuffer = new MetricBuffer(new Attributes().put("key1", "val1"));
    metricBuffer.addMetric(
        new Count("myCounter", 1, currentTimeMillis, currentTimeMillis + 42, countAttributes));

    assertThrows(
        RetryWithBackoffException.class,
        () -> metricBatchSender.sendBatch(metricBuffer.createBatch()));
  }

  private static Stream<Arguments> codesAndExpectedExceptions() {
    return Stream.of(
        Arguments.of(
            413,
            RetryWithSplitException.class,
            "Batch was too large. Please try splitting and resending smaller sized batches."),
        Arguments.of(
            420,
            RetryWithBackoffException.class,
            "The New Relic API suggests backing off exponentially on this request."),
        Arguments.of(
            403,
            DiscardBatchException.class,
            "The New Relic API failed to process this request and it should not be retried."),
        Arguments.of(
            404,
            DiscardBatchException.class,
            "The New Relic API failed to process this request and it should not be retried."),
        Arguments.of(429, RetryWithRequestedWaitException.class, "Please retry after 10 SECONDS"),
        Arguments.of(
            500,
            RetryWithBackoffException.class,
            "The New Relic API suggests backing off exponentially on this request."),
        Arguments.of(
            503,
            RetryWithBackoffException.class,
            "The New Relic API suggests backing off exponentially on this request."));
  }

  @ParameterizedTest
  @MethodSource("codesAndExpectedExceptions")
  @DisplayName("SDK responds appropriately to non-202s by suggesting a course of action.")
  void testMetricSendResponseCodes(
      int statusCode, Class<? extends ResponseException> exceptionClass, String exceptionMessage)
      throws Exception {
    mockServerClient
        .when(
            new HttpRequest()
                .withMethod("POST")
                .withPath("/metric/v1")
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withHeader("Content-Length", ".*"))
        .respond(new HttpResponse().withStatusCode(statusCode).withBody("" + statusCode));

    Attributes countAttributes = new Attributes().put("key2", "val2");

    long currentTimeMillis = 350;

    MetricBuffer metricBuffer = new MetricBuffer(new Attributes().put("key1", "val1"));
    metricBuffer.addMetric(
        new Count("myCounter", 1, currentTimeMillis, currentTimeMillis + 42, countAttributes));
    assertThrows(
        exceptionClass,
        () -> metricBatchSender.sendBatch(metricBuffer.createBatch()),
        exceptionMessage);
  }

  //
  // [{\"common\":{\"attributes\":{\"key1\":\"val1\"}},\"metrics\":[{\"name\":\"myCounter\",\"type\":\"count\",\"value\":1.0,\"timestamp\":1557180766612,\"interval.ms\":42,\"attributes\":{\"key2\":\"val2\"}}]}]"

  private static class MetricPayload {
    private final Map<String, Object> common;
    private final List<Map<String, Object>> metrics;

    public MetricPayload(Map<String, Object> singletonMap, List<Map<String, Object>> asList) {
      common = singletonMap;
      metrics = asList;
    }

    public Map<String, Object> getCommon() {
      return common;
    }

    public List<Map<String, Object>> getMetrics() {
      return metrics;
    }
  }
}
