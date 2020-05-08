/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.examples;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.OkHttpPoster;
import com.newrelic.telemetry.SenderConfiguration;
import com.newrelic.telemetry.logs.Log;
import com.newrelic.telemetry.logs.LogBatch;
import com.newrelic.telemetry.logs.LogBatchSender;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is an example of sending a batch of Logs to New Relic.
 *
 * <p>A LogBatchSender is created with the Insights insert key and the reference http implementation
 * from OkHttp. An example batch of 4 logs (apples, oranges, beer, wine) is created and then sent
 * via sender.sendBatch().
 *
 * <p>To run this example, pass the insights api key as a commandline argument.
 */
public class LogExample {
  private static final Logger logger = LoggerFactory.getLogger(LogExample.class);

  private static final ThreadLocalRandom random = ThreadLocalRandom.current();

  private static final List<String> items = Arrays.asList("apples", "oranges", "beer", "wine");

  public static void main(String[] args) throws Exception {
    logger.info("Starting the LogExample");
    String insightsInsertKey = args[0];

    SenderConfiguration configuration =
        LogBatchSender.configurationBuilder()
            .apiKey(insightsInsertKey)
            .auditLoggingEnabled(true)
            .httpPoster(new OkHttpPoster())
            .build();
    LogBatchSender sender = LogBatchSender.create(configuration);

    List<Log> logs = new ArrayList<>();
    logs.add(Log.builder().level("INFO").message("Start of process").build());
    for (String item : items) {
      String logId = UUID.randomUUID().toString();
      Attributes attributes = new Attributes().put("id", logId).put("food", item);
      logs.add(
          Log.builder()
              .attributes(attributes)
              .message("Processing " + item)
              .level("DEBUG")
              .build());
      if (new Random().nextBoolean()) {
        logs.add(
            Log.builder()
                .attributes(attributes)
                .level("ERROR")
                .stackTrace(makeException(item))
                .build());
      }
      logs.add(
          Log.builder()
              .attributes(attributes)
              .message("Done processing " + item)
              .level("DEBUG")
              .build());
    }
    logs.add(Log.builder().level("INFO").message("End of process").build());

    sender.sendBatch(new LogBatch(logs, getCommonAttributes()));
  }

  private static Exception makeException(String item) {
    return aLevelDeeper(item);
  }

  private static Exception aLevelDeeper(String item) {
    return new Exception("Exceptional things with the " + item + "!");
  }

  /** These attributes are shared across all logs submitted in the batch. */
  private static Attributes getCommonAttributes() {
    return new Attributes()
        .put("exampleName", "LogExample")
        .put("service.name", "Telemetry SDK Log Example");
  }
}
