/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockserver.model.JsonBody.json;

import com.google.common.collect.ImmutableMap;
import com.google.common.net.MediaType;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockserver.client.MockServerClient;
import org.mockserver.matchers.MatchType;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.startupcheck.MinimumDurationRunningStartupCheckStrategy;
import org.testcontainers.containers.wait.strategy.WaitAllStrategy;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class SpanApiIntegrationTest {

  private static final int SERVICE_PORT = 1080 + new Random().nextInt(900);
  private static String containerIpAddress;
  private static MockServerClient mockServerClient;
  private static final GenericContainer<?> container =
      new GenericContainer<>("jamesdbloom/mockserver:mockserver-5.5.1")
          .withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()))
          .withExposedPorts(SERVICE_PORT);
  private static URL endpointUrl;
  private SpanBatchSender spanBatchSender;

  @BeforeAll
  static void beforeClass() throws MalformedURLException {
    container.setPortBindings(singletonList(SERVICE_PORT + ":1080"));
    container.setWaitStrategy(new WaitAllStrategy());
    container.setStartupCheckStrategy(
        new MinimumDurationRunningStartupCheckStrategy(Duration.of(10, SECONDS)));
    container.start();
    containerIpAddress = container.getContainerIpAddress();
    mockServerClient = new MockServerClient(containerIpAddress, SERVICE_PORT);
    endpointUrl = new URL("http://" + containerIpAddress + ":" + SERVICE_PORT + "/trace/v1");
  }

  @BeforeEach
  void setUp() throws Exception {
    mockServerClient.reset();
    SpanBatchSenderFactory factory =
        SpanBatchSenderFactory.fromHttpImplementation(OkHttpPoster::new);
    SenderConfiguration config =
        factory
            .configureWith("fakeKey")
            .httpPoster(new OkHttpPoster(Duration.ofMillis(1500)))
            .endpoint(endpointUrl)
            .auditLoggingEnabled(true)
            .secondaryUserAgent("myTestApp")
            .build();
    spanBatchSender = SpanBatchSender.create(config);
  }

  @Test
  @DisplayName("Low Level SDK can send a span batch and returns a successful response")
  void testSuccessfulSpanSend() throws Exception {
    SpanPayload expectedPayload =
        new SpanPayload(
            ImmutableMap.of("trace.id", "123456", "attributes", singletonMap("key1", "val1")),
            singletonList(
                ImmutableMap.<String, Object>builder()
                    .put("id", "6f377f46-3f10-4e8e-8390-7717044cdbe9")
                    .put("trace.id", "123456")
                    .put("timestamp", 55555)
                    .put(
                        "attributes",
                        ImmutableMap.of(
                            "duration.ms",
                            60,
                            "service.name",
                            "Span Test Service",
                            "name",
                            "spanTest"))
                    .build()));

    mockServerClient
        .when(
            new HttpRequest()
                .withMethod("POST")
                .withPath("/trace/v1")
                .withBody(
                    json(
                        new SpanPayload[] {expectedPayload},
                        MediaType.JSON_UTF_8,
                        MatchType.STRICT))
                .withHeader("User-Agent", "NewRelic-Java-TelemetrySDK/.* myTestApp")
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withHeader("Content-Length", ".*"))
        .respond(new HttpResponse().withStatusCode(202));

    List<Span> spans = new ArrayList<>();
    spans.add(
        Span.builder("6f377f46-3f10-4e8e-8390-7717044cdbe9")
            .traceId("123456")
            .name("spanTest")
            .durationMs(60)
            .timestamp(55555)
            .serviceName("Span Test Service")
            .build());
    Response response =
        spanBatchSender.sendBatch(new SpanBatch(spans, getCommonAttributes(), "123456"));

    assertEquals(202, response.getStatusCode());
    assertEquals("Accepted", response.getStatusMessage());
  }

  private static Attributes getCommonAttributes() {
    return new Attributes().put("key1", "val1");
  }

  /*
  [{"common":{"trace.id":"144cbde7-65f0-4043-8d5d-e70cce479d89","attributes":{"exampleName":"SpanExample"}},
  "spans":[{"id":"6f377f46-3f10-4e8e-8390-7717044cdbe9","trace.id":"144cbde7-65f0-4043-8d5d-e70cce479d89","timestamp":1567707504897,"attributes":{"duration.ms":62.0,"service.name":"Telemetry SDK Span Example (apples)","name":"apples"}}]}]
   */

  private static class SpanPayload {
    private final Map<String, Object> common;
    private final List<Map<String, Object>> spans;

    public SpanPayload(ImmutableMap<String, Object> of, List<Map<String, Object>> singletonList) {
      common = of;
      spans = singletonList;
    }

    public Map<String, Object> getCommon() {
      return common;
    }

    public List<Map<String, Object>> getSpans() {
      return spans;
    }
  }
}
