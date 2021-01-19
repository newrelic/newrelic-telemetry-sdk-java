package com.newrelic.telemetry.examples;

import com.newrelic.telemetry.core.Attributes;
import com.newrelic.telemetry.core.SpanBatchSenderFactory;
import com.newrelic.telemetry.core.exceptions.ResponseException;
import com.newrelic.telemetry.core.spans.Span;
import com.newrelic.telemetry.core.spans.SpanBatch;
import com.newrelic.telemetry.core.spans.SpanBatchSender;
import com.newrelic.telemetry.okhttp.OkHttpPoster;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SpanToTraceObserverExample {
  private static final Logger logger = LoggerFactory.getLogger(SpanExample.class);

  private static final ThreadLocalRandom random = ThreadLocalRandom.current();

  private static final List<String> items =
      Arrays.asList("apples", "oranges", "papayas", "mangoes");

  public static void main(String[] args) throws ResponseException, MalformedURLException {
    logger.info("Starting the SpanToTraceObserverExample");
    String apiInsertKey = args[0];
    String apiUrl = args[1];

    if (apiInsertKey != null && apiUrl != null) {
      SpanBatchSender sender =
          SpanBatchSender.create(
              SpanBatchSenderFactory.fromHttpImplementation(OkHttpPoster::new)
                  .configureWith(apiInsertKey)
                  .auditLoggingEnabled(true)
                  .endpoint(new URL(apiUrl))
                  .build());

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
                .serviceName("Telemetry SDK Span TraceObserver Example (" + item + ")")
                .build());
        spanStartTime += durationMs;
        parentId = spanId;
      }

      sender.sendBatch(
          new SpanBatch(
              spans, new Attributes().put("TraceObserver", "TraceObserverSpanExample"), traceId));
    }
  }
}
