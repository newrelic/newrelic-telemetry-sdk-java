/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.TelemetryBatch;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Collection;
import java.util.Collections;

/** Represents a set of {@link Metric} instances, to be sent up to the New Relic Metrics API. */
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class MetricBatch extends TelemetryBatch<Metric> {

  public MetricBatch(Collection<Metric> metrics, Attributes commonAttributes) {
    super(metrics, commonAttributes);
  }

  @Override
  public TelemetryBatch<Metric> createSubBatch(Collection<Metric> telemetry) {
    return new MetricBatch(telemetry, getCommonAttributes());
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private String instrumentationProvider;
    private String serviceName;
    private Collection<Metric> metrics = Collections.emptyList();
    private CommonAttributesBuilder commonAttributesBuilder = new CommonAttributesBuilder();

    /**
     * Optional. Specify the name of the service that is creating the metrics. The service name will
     * be included in all common attributes.
     *
     * @param serviceName - The name of the service
     */
    public Builder serviceName(String serviceName) {
      commonAttributesBuilder.serviceName(serviceName);
      return this;
    }

    /**
     * Optional. Specify the name of the instrumentation that provides the metrics. The
     * instrumentation provider will be included in all common attributes.
     *
     * @param instrumentationProvider - The instrumentation provider name
     */
    public Builder instrumentationProvider(String instrumentationProvider) {
      commonAttributesBuilder.instrumentationProvider(instrumentationProvider);
      return this;
    }

    public Builder attributes(Attributes attributes) {
      commonAttributesBuilder.attributes(attributes);
      return this;
    }

    public Builder metrics(Collection<Metric> metrics) {
      this.metrics = metrics;
      return this;
    }

    public MetricBatch build() {
      Attributes attributes = commonAttributesBuilder.build();
      return new MetricBatch(metrics, attributes);
    }
  }
}
