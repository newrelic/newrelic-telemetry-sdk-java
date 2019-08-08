/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import com.newrelic.telemetry.MetricBatchSender.Builder;
import java.time.Duration;

public class SimpleMetricBatchSender {

  public static Builder builder(String apiKey) {
    return builder(apiKey, Duration.ZERO);
  }

  public static Builder builder(String apiKey, Duration callTimeout) {
    OkHttpPoster okHttpPoster = new OkHttpPoster(callTimeout);
    return MetricBatchSender.builder(apiKey, okHttpPoster, new MetricGsonGenerator());
  }
}
