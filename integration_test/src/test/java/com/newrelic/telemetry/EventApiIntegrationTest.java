/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockserver.model.JsonBody.json;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;
import com.newrelic.telemetry.events.Event;
import com.newrelic.telemetry.events.EventBatchSender;
import com.newrelic.telemetry.events.EventBuffer;
import com.newrelic.telemetry.exceptions.*;
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
import org.mockserver.model.JsonBody;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class EventApiIntegrationTest {

  private static final int SERVICE_PORT = 1080 + new Random().nextInt(900);
  private static String containerIpAddress;
  private static MockServerClient mockServerClient;

  private static final GenericContainer<?> container =
      new GenericContainer<>("jamesdbloom/mockserver:mockserver-5.5.1")
          .withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()))
          .withExposedPorts(SERVICE_PORT);

  private EventBatchSender eventBatchSender;

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
    EventBatchSenderFactory factory =
        EventBatchSenderFactory.fromHttpImplementation(OkHttpPoster::new);
    SenderConfiguration config =
        factory
            .configureWith("fakeKey", Duration.ofMillis(1500))
            .endpointUrl(URI.create("http://" + containerIpAddress + ":" + SERVICE_PORT).toURL())
            .secondaryUserAgent("testApplication/1.0.0")
            .build();
    eventBatchSender = EventBatchSender.create(config);
  }

  @Test
  @DisplayName("Low Level SDK can send a metric batch and returns a successful response")
  void testSuccessfulMetricSend() throws Exception {
    List<Map<String, Object>> expectedPayload =
        Arrays.asList(
            ImmutableMap.<String, Object>builder()
                .put("eventType", "myEvent")
                .put("key1", "val1")
                .put("timestamp", 350)
                .build());
    JsonBody json = json(expectedPayload, MediaType.JSON_UTF_8, MatchType.STRICT);

    mockServerClient
        .when(
            new HttpRequest()
                .withMethod("POST")
                .withPath("/v1/accounts/events")
                .withBody(json)
                .withHeader("User-Agent", "NewRelic-Java-TelemetrySDK/.* testApplication/1.0.0")
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withHeader("Content-Length", ".*"))
        .respond(new HttpResponse().withStatusCode(202));

    long currentTimeMillis = 350;

    EventBuffer eventBuffer = new EventBuffer(new Attributes());
    eventBuffer.addEvent(
        new Event("myEvent", new Attributes().put("key1", "val1"), currentTimeMillis));

    Response response = eventBatchSender.sendBatch(eventBuffer.createBatch());

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
                .withPath("/v1/accounts/events")
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withHeader("Content-Length", ".*"))
        .respond(new HttpResponse().withStatusCode(202).withDelay(new Delay(TimeUnit.SECONDS, 2)));

    long currentTimeMillis = 350;

    EventBuffer eventBuffer = new EventBuffer(new Attributes());
    eventBuffer.addEvent(
        new Event("myEvent", new Attributes().put("key1", "val1"), currentTimeMillis));

    assertThrows(
        RetryWithBackoffException.class,
        () -> eventBatchSender.sendBatch(eventBuffer.createBatch()));
  }

  private static Stream<Arguments> codesAndExpectedExceptions() {
    return Stream.of(
        //        Arguments.of(
        //            413,
        //            RetryWithSplitException.class,
        //            "Batch was too large. Please try splitting and resending smaller sized
        // batches."),
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
                .withPath("/v1/accounts/events")
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withHeader("Content-Length", ".*"))
        .respond(new HttpResponse().withStatusCode(statusCode).withBody("" + statusCode));

    long currentTimeMillis = 350;

    EventBuffer eventBuffer = new EventBuffer(new Attributes());
    eventBuffer.addEvent(
        new Event("myEvent", new Attributes().put("key1", "val1"), currentTimeMillis));
    eventBuffer.addEvent(
        new Event("myEvent", new Attributes().put("key2", "val2"), currentTimeMillis + 1));
    assertThrows(
        exceptionClass,
        () -> eventBatchSender.sendBatch(eventBuffer.createBatch()),
        exceptionMessage);
  }
}
