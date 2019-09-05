/*
 * --------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.examples;

import com.google.gson.Gson;
import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.AttributesGson;
import com.newrelic.telemetry.OkHttpPoster;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an example of sending a batch of Spans to New Relic.
 *
 * <p>A SpanBatchSender is created with the Insights insert key and reference implementations from
 * Gson and OkHttp. An example batch of 4 spans (apples, oranges, beer, wine) is created and then
 * sent via sender.sendBatch().
 *
 * <p>To run this example, pass the insights api key as a commandline argument.
 */
public class SpanExample {
  private static final Logger logger = LoggerFactory.getLogger(SpanExample.class);

  private static final ThreadLocalRandom random = ThreadLocalRandom.current();

  private static final List<String> items = Arrays.asList("apples", "oranges", "beer", "wine");

  public static void main(String[] args) throws Exception {
    logger.info("Starting the SpanExample");
    String insightsInsertKey = args[0];

    SpanBatchSender sender =
        SpanBatchSender.builder(
                insightsInsertKey,
                new OkHttpPoster(Duration.ofSeconds(5)),
                new AttributesGson(new Gson()))
            .enableAuditLogging()
            .build();

    List<Span> spans = new ArrayList<>();
    String traceId = UUID.randomUUID().toString();
    long spanStartTime = System.currentTimeMillis();
    String parentId = null;
    for (String item : items) {
      int durationMs = random.nextInt(1000);

      String spanId = UUID.randomUUID().toString();
      spans.add(
          Span.builder(spanId)
              .traceId(traceId)
              .name(item)
              .parentId(parentId)
              .durationMs(durationMs)
              .timestamp(spanStartTime)
              .serviceName("Telemetry SDK Span Example (" + item + ")")
              .build());
      spanStartTime += durationMs;
      parentId = spanId;
    }

    sender.sendBatch(new SpanBatch(spans, getCommonAttributes(), traceId));
  }

  /** These attributes are shared across all metrics submitted in the batch. */
  private static Attributes getCommonAttributes() {
    return new Attributes().put("exampleName", "SpanExample");
  }
}
