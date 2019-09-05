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
 * This example shows an example of generating a Count metric. Count metrics have two requirements:
 *
 * <p>1) They represent the "delta" in the counted value from the previous report.
 *
 * <p>2) They must include the time range over which the delta has accumulated.
 *
 * <p>Additionally, this provides an example of using a {@code
 * com.newrelic.telemetry.metrics.MetricBuffer} to hold on to metrics and send them as a batch.
 *
 * <p>To run this example, provide 2 command line args, the first is the URL to the metric ingest
 * endpoint, and the 2nd is the Insights Insert key.
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
