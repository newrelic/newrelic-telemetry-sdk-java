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
import com.newrelic.telemetry.logs.Log;
import com.newrelic.telemetry.logs.LogBatch;
import com.newrelic.telemetry.logs.LogBatchSender;
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
class LogApiIntegrationTest {

  private static final int SERVICE_PORT = 1080 + new Random().nextInt(900);
  private static String containerIpAddress;
  private static MockServerClient mockServerClient;
  private static final GenericContainer<?> container =
      new GenericContainer<>("jamesdbloom/mockserver:mockserver-5.5.1")
          .withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()))
          .withExposedPorts(SERVICE_PORT);
  private static URL endpointUrl;
  private LogBatchSender logBatchSender;

  @BeforeAll
  static void beforeClass() throws MalformedURLException {
    container.setPortBindings(singletonList(SERVICE_PORT + ":1080"));
    container.setWaitStrategy(new WaitAllStrategy());
    container.setStartupCheckStrategy(
        new MinimumDurationRunningStartupCheckStrategy(Duration.of(10, SECONDS)));
    container.start();
    containerIpAddress = container.getContainerIpAddress();
    mockServerClient = new MockServerClient(containerIpAddress, SERVICE_PORT);
    endpointUrl = new URL("http://" + containerIpAddress + ":" + SERVICE_PORT + "/log/v1");
  }

  @BeforeEach
  void setUp() throws Exception {
    mockServerClient.reset();
    SenderConfiguration config =
        LogBatchSenderFactory.fromHttpImplementation(OkHttpPoster::new)
            .configureWith("fakeKey")
            .httpPoster(new OkHttpPoster(Duration.ofMillis(1500)))
            .endpoint(endpointUrl)
            .auditLoggingEnabled(true)
            .secondaryUserAgent("myTestApp")
            .build();
    logBatchSender = LogBatchSender.create(config);
  }

  @Test
  @DisplayName("Low Level SDK can send a log batch and returns a successful response")
  void testSuccessfulSpanSend() throws Exception {
    LogPayload expectedPayload =
        new LogPayload(
            ImmutableMap.of("attributes", singletonMap("key1", "val1")),
            singletonList(
                ImmutableMap.<String, Object>builder()
                    .put("timestamp", 55555)
                    .put("message", "log message goes here")
                    .put(
                        "attributes",
                        ImmutableMap.of("service.name", "Log Test Service", "log.level", "DEBUG"))
                    .build()));

    mockServerClient
        .when(
            new HttpRequest()
                .withMethod("POST")
                .withPath("/log/v1")
                .withBody(
                    json(
                        new LogPayload[] {expectedPayload}, MediaType.JSON_UTF_8, MatchType.STRICT))
                .withHeader("User-Agent", "NewRelic-Java-TelemetrySDK/.* myTestApp")
                .withHeader("Content-Type", "application/json; charset=utf-8")
                .withHeader("Content-Length", ".*"))
        .respond(new HttpResponse().withStatusCode(202));

    List<Log> logs = new ArrayList<>();
    logs.add(
        Log.builder()
            .message("log message goes here")
            .level("DEBUG")
            .timestamp(55555)
            .serviceName("Log Test Service")
            .build());
    Response response = logBatchSender.sendBatch(new LogBatch(logs, getCommonAttributes()));

    assertEquals(202, response.getStatusCode());
    assertEquals("Accepted", response.getStatusMessage());
  }

  private static Attributes getCommonAttributes() {
    return new Attributes().put("key1", "val1");
  }

  /*
  [{"common":{"attributes":{"key1":"val1","exampleName":"LogExample"}},
    "logs":[{"timestamp":55555,"attributes":{"log.level":"DEBUG"},"message":"log message goes here"}]}]
   */

  private static class LogPayload {
    private final Map<String, Object> common;
    private final List<Map<String, Object>> logs;

    public LogPayload(
        ImmutableMap<String, Object> commonAttributes, List<Map<String, Object>> data) {
      common = commonAttributes;
      logs = data;
    }

    public Map<String, Object> getCommon() {
      return common;
    }

    public List<Map<String, Object>> getLogs() {
      return logs;
    }
  }
}
