/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.examples;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.OkHttpPoster;
import com.newrelic.telemetry.SenderConfiguration;
import com.newrelic.telemetry.SpanBatchSenderFactory;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
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
 * <p>A SpanBatchSender is created with the Insights insert key and the reference http
 * implementation from OkHttp. An example batch of 4 spans (apples, oranges, beer, wine) is created
 * and then sent via sender.sendBatch().
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

    SpanBatchSenderFactory factory =
        SpanBatchSenderFactory.fromHttpImplementation(OkHttpPoster::new);
    SenderConfiguration configuration =
        factory.configureWith(insightsInsertKey).auditLoggingEnabled(true).build();
    SpanBatchSender sender = SpanBatchSender.create(configuration);

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

  /** These attributes are shared across all spans submitted in the batch. */
  private static Attributes getCommonAttributes() {
    return new Attributes().put("exampleName", "SpanExample");
  }
}
