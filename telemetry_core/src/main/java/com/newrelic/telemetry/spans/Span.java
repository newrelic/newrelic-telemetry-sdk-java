/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.util.Utils;

/**
 * Spans are New Relic's analog of OpenTracing spans. They can represent events such as external
 * calls, individual operations, and datastore calls.
 */
public final class Span implements Telemetry {

  private final String id;
  private final Attributes attributes;

  private final String traceId; // trace.id <- top level
  private final long timestamp; // in epoch ms

  private final String serviceName; // service.name <- goes in attributes
  private final Double durationMs; // duration.ms <- goes in attributes
  private final String name; // goes in attributes
  private final String parentId; // parent.id <- goes in attributes
  private final boolean error;

  private Span(SpanBuilder spanBuilder) {
    Utils.verifyNonNull(spanBuilder.id, "id");
    this.id = spanBuilder.id;
    this.attributes = spanBuilder.attributes;
    this.traceId = spanBuilder.traceId;
    this.timestamp = spanBuilder.timestamp;
    this.serviceName = spanBuilder.serviceName;
    this.durationMs = spanBuilder.durationMs;
    this.name = spanBuilder.name;
    this.parentId = spanBuilder.parentId;
    this.error = spanBuilder.error;
  }

  /**
   * @param spanId The ID associated with this span
   * @return A Builder class that can be used to add variables to a Span object and create a new
   *     Span instance
   */
  public static SpanBuilder builder(String spanId) {
    return new SpanBuilder(spanId);
  }

  public String getId() {
    return id;
  }

  public Attributes getAttributes() {
    return attributes;
  }

  public String getTraceId() {
    return traceId;
  }

  public long getTimestamp() {
    return timestamp;
  }

  public String getServiceName() {
    return serviceName;
  }

  public Double getDurationMs() {
    return durationMs;
  }

  public String getName() {
    return name;
  }

  public String getParentId() {
    return parentId;
  }

  public boolean isError() {
    return error;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Span span = (Span) o;

    if (getTimestamp() != span.getTimestamp()) return false;
    if (isError() != span.isError()) return false;
    if (getId() != null ? !getId().equals(span.getId()) : span.getId() != null) return false;
    if (getAttributes() != null
        ? !getAttributes().equals(span.getAttributes())
        : span.getAttributes() != null) return false;
    if (getTraceId() != null ? !getTraceId().equals(span.getTraceId()) : span.getTraceId() != null)
      return false;
    if (getServiceName() != null
        ? !getServiceName().equals(span.getServiceName())
        : span.getServiceName() != null) return false;
    if (getDurationMs() != null
        ? !getDurationMs().equals(span.getDurationMs())
        : span.getDurationMs() != null) return false;
    if (getName() != null ? !getName().equals(span.getName()) : span.getName() != null)
      return false;
    return getParentId() != null
        ? getParentId().equals(span.getParentId())
        : span.getParentId() == null;
  }

  @Override
  public int hashCode() {
    int result = getId() != null ? getId().hashCode() : 0;
    result = 31 * result + (getAttributes() != null ? getAttributes().hashCode() : 0);
    result = 31 * result + (getTraceId() != null ? getTraceId().hashCode() : 0);
    result = 31 * result + (int) (getTimestamp() ^ (getTimestamp() >>> 32));
    result = 31 * result + (getServiceName() != null ? getServiceName().hashCode() : 0);
    result = 31 * result + (getDurationMs() != null ? getDurationMs().hashCode() : 0);
    result = 31 * result + (getName() != null ? getName().hashCode() : 0);
    result = 31 * result + (getParentId() != null ? getParentId().hashCode() : 0);
    result = 31 * result + (isError() ? 1 : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Span{"
        + "id='"
        + id
        + '\''
        + ", attributes="
        + attributes
        + ", traceId='"
        + traceId
        + '\''
        + ", timestamp="
        + timestamp
        + ", serviceName='"
        + serviceName
        + '\''
        + ", durationMs="
        + durationMs
        + ", name='"
        + name
        + '\''
        + ", parentId='"
        + parentId
        + '\''
        + ", error="
        + error
        + '}';
  }

  /**
   * A class for holding the variables associated with a Span object and creating a new Span object
   * with those variables
   */
  public static class SpanBuilder {

    private String id;
    private Attributes attributes = new Attributes();
    private String traceId;
    private long timestamp = System.currentTimeMillis();
    private String serviceName;
    private Double durationMs;
    private String name;
    private String parentId;
    private boolean error = false;

    /** @param spanId The ID associated with the Span object to be created */
    SpanBuilder(String spanId) {
      this.id = spanId;
    }

    /**
     * @param attributes Dimensional attributes as key-value pairs, associated with the Span object
     *     to be created. See {@link Attributes}
     * @return The SpanBuilder object with its attributes variable set to the given attributes
     *     object
     */
    public SpanBuilder attributes(Attributes attributes) {
      this.attributes = attributes;
      return this;
    }

    /**
     * @param traceId The ID used to identify a request as it crosses process boundaries, and in
     *     turn link span events
     * @return The SpanBuilder object with its traceId variable set to the given Trace Id
     */
    public SpanBuilder traceId(String traceId) {
      this.traceId = traceId;
      return this;
    }

    /**
     * @param timestamp The start time of the span event in epoch milliseconds
     * @return The SpanBuilder object with its timestamp variable set to the given timestamp
     */
    public SpanBuilder timestamp(long timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    /**
     * @param serviceName The name of the service this Span event occurred in
     * @return The SpanBuilder object with its serviceName variable set to the given Service Name
     */
    public SpanBuilder serviceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    /**
     * @param durationMs The duration of the Span event, in milliseconds
     * @return The SpanBuilder object with its durationMs variable set to the given duration
     */
    public SpanBuilder durationMs(double durationMs) {
      this.durationMs = durationMs;
      return this;
    }

    /**
     * @param name The name of the Span event
     * @return The SpanBuilder object with its name variable set to the given name
     */
    public SpanBuilder name(String name) {
      this.name = name;
      return this;
    }

    /**
     * @param parentId The Id of the parent span for this Span event. If it is a root span, this
     *     variable should stay null, or not set
     * @return The SpanBuilder object with its parentId variable set to the given Parent ID
     */
    public SpanBuilder parentId(String parentId) {
      this.parentId = parentId;
      return this;
    }

    /**
     * Call this to indicate that the span contains an error condition.
     *
     * @return The SpanBuilder instance with the error field set to true
     */
    public SpanBuilder withError() {
      return withError(null, null);
    }

    /**
     * Call this to indicate that the span contains an error condition with the given message.
     *
     * @param errorMessage The error message to be placed into the "error.message" attribute
     * @return The SpanBuilder instance with the error field set to true
     */
    public SpanBuilder withError(String errorMessage) {
      return withError(errorMessage, null);
    }

    /**
     * Call this to indicate that the span contains an error condition with the given message and
     * class.
     *
     * @param errorMessage The error message to be placed into the "error.message" attribute
     * @param errorClass The error class to be placed into the "error.class" attribute
     * @return The SpanBuilder instance with the error field set to true
     */
    public SpanBuilder withError(String errorMessage, String errorClass) {
      this.error = true;
      if (errorMessage != null) {
        attributes.put("error.message", errorMessage);
      }
      if (errorClass != null) {
        attributes.put("error.class", errorClass);
      }
      return this;
    }

    /** @return A Span object with the variables assigned to the builder class */
    public Span build() {
      return new Span(this);
    }

    /** @return A string representing this SpanBuilder object and listing its variables */
    @Override
    public String toString() {
      return "SpanBuilder{"
          + "id='"
          + id
          + '\''
          + ", attributes="
          + attributes
          + ", traceId='"
          + traceId
          + '\''
          + ", timestamp="
          + timestamp
          + ", serviceName='"
          + serviceName
          + '\''
          + ", durationMs="
          + durationMs
          + ", name='"
          + name
          + '\''
          + ", parentId='"
          + parentId
          + '\''
          + ", error="
          + error
          + '}';
    }
  }
}
