package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.Attributes;

/** A builder for Attributes. It provides convenience names for commonly used attributes. */
public class CommonAttributesBuilder {

  private Attributes attributes = new Attributes();
  private String serviceName;
  private String instrumentationProvider;

  /**
   * Provides a new set of attributes for this builder
   *
   * @param attributes - the new attributes
   * @return this builder
   */
  public CommonAttributesBuilder attributes(Attributes attributes) {
    this.attributes = attributes;
    return this;
  }

  /**
   * Specifies the service name of the current application. This value will end up in the
   * "service.name" attribute.
   *
   * @param serviceName - The name of the service.
   * @return this builder
   */
  public CommonAttributesBuilder serviceName(String serviceName) {
    this.serviceName = serviceName;
    return this;
  }

  /**
   * Specifies the name of the provider of the instrumentation to this builder, for example
   * "micrometer" or "opentelemetry". Users doing manual instrumentation should generally not call
   * this method. This value is intended for use by instrumentation frameworks. The value will end
   * up in the "instrumentation.provider" attribute.
   *
   * @param instrumentationProvider - The instrumentation provider name
   * @return this builder
   */
  public CommonAttributesBuilder instrumentationProvider(String instrumentationProvider) {
    this.instrumentationProvider = instrumentationProvider;
    return this;
  }

  /**
   * Call this lastly to actually build an Attributes instance that optionally contains specially
   * named fields.
   *
   * @return a fresh new Attributes instance
   */
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
