package com.newrelic.telemetry.examples;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.OkHttpPoster;
import com.newrelic.telemetry.SpanBatchSenderFactory;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.spans.Span;
import com.newrelic.telemetry.spans.SpanBatch;
import com.newrelic.telemetry.spans.SpanBatchSender;
import com.newrelic.telemetry.util.Utils;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    String licenseKey = args[0];
    String traceObserverUrl = args[1];

    if (licenseKey != null && traceObserverUrl != null) {
      SpanBatchSender sender =
          SpanBatchSender.create(
              SpanBatchSenderFactory.fromHttpImplementation(OkHttpPoster::new)
                  .configureWith(licenseKey)
                  .useLicenseKey(true)
                  .auditLoggingEnabled(true)
                  .endpoint(new URL(traceObserverUrl))
                  .build());

      List<Span> spans = new ArrayList<>();
      String traceId = Utils.generateUUID().toString();
      long spanStartTime = System.currentTimeMillis();
      String parentId = null;
      for (String item : items) {
        int durationMs = random.nextInt(1000);
        String spanId = Utils.generateUUID().toString();
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
