/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import com.newrelic.telemetry.metrics.MetricBatchSender;
import com.newrelic.telemetry.metrics.MetricBatchSenderBuilder;
import java.time.Duration;

public class SimpleMetricBatchSender {

  public static MetricBatchSender build(String apiKey) {
    return builder(apiKey).build();
  }

  public static MetricBatchSenderBuilder builder(String apiKey) {
    return builder(apiKey, Duration.ofSeconds(2));
  }

  public static MetricBatchSender build(String apiKey, Duration callTimeout) {
    return builder(apiKey, callTimeout).build();
  }

  public static MetricBatchSenderBuilder builder(String apiKey, Duration callTimeout) {
    OkHttpPoster okHttpPoster = new OkHttpPoster(callTimeout);
    return MetricBatchSender.builder().apiKey(apiKey).httpPoster(okHttpPoster);
  }
}
