package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.SenderConfiguration.SenderConfigurationBuilder;

/**
 * Note: This class is deprecated and will be removed in the next major version - you should move to
 * the factories in telemetry-core
 */
@Deprecated
public class SpanBatchSenderBuilder {
  private final SenderConfigurationBuilder configBuilder;

  public SpanBatchSenderBuilder(SenderConfigurationBuilder configurationBuilder) {
    configBuilder = configurationBuilder;
  }

  public SpanBatchSender build() {
    return SpanBatchSender.create(configBuilder.build());
  }
}
