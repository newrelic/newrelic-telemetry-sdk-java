package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.SenderConfiguration.SenderConfigurationBuilder;

/**
 * Note: This class is deprecated and will be removed in the next major version - you should move to
 * the factories in telemetry-core
 */
@Deprecated
public class MetricBatchSenderBuilder {
  private final SenderConfigurationBuilder configBuilder;

  public MetricBatchSenderBuilder(SenderConfigurationBuilder senderConfigurationBuilder) {
    configBuilder = senderConfigurationBuilder;
  }

  public MetricBatchSender build() {
    return MetricBatchSender.create(configBuilder.build());
  }
}
