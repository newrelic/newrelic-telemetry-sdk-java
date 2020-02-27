package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.Attributes;

/** Encapsulates the building of commonly used attributes. */
public class CommonAttributesBuilder {

  private Attributes attributes = new Attributes();
  private String serviceName;
  private String instrumentationProvider;

  public CommonAttributesBuilder attributes(Attributes attributes) {
    this.attributes = attributes;
    return this;
  }

  public CommonAttributesBuilder serviceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  public CommonAttributesBuilder instrumentationProvider(String instrumentationProvider) {
    this.instrumentationProvider = instrumentationProvider;
    return this;
  }

  public Attributes build() {
    if (serviceName != null) {
      attributes.put("service.name", serviceName);
    }
    if (instrumentationProvider != null) {
      attributes.put("instrumentation.provider", instrumentationProvider);
    }
    return attributes;
  }
}
