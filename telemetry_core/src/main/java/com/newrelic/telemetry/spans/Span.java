/*
 * --------------------------------------------------------------------------------------------
 *   Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *   Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 *  --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.util.Utils;
import lombok.Value;

/**
 * Spans are New Relic's analog of OpenTracing spans. They can represent events such as external
 * call,s individual operations, and datastore calls.
 */
@Value
public final class Span implements Telemetry {

  private final String id;
  private final Attributes attributes;

  private final String traceId; // trace.id <- top level
  private final long timestamp; // in epoch ms

  private final String serviceName; // service.name <- goes in attributes
  private final double durationMs; // duration.ms <- goes in attributes
  private final String name; // goes in attributes
  private final String parentId; // parent.id <- goes in attributes

  private Span(
      String id,
      Attributes attributes,
      String traceId,
      long timestamp,
      String serviceName,
      double durationMs,
      String name,
      String parentId) {
    Utils.verifyNonNull(id, "id");
    this.id = id;
    this.attributes = attributes;
    this.traceId = traceId;
    this.timestamp = timestamp;
    this.serviceName = serviceName;
    this.durationMs = durationMs;
    this.name = name;
    this.parentId = parentId;
  }

  /**
   * @param spanId The ID associated with this span
   * @return A Builder class that can be used to add variables to a Span object and create a new
   *     Span instance
   */
  public static SpanBuilder builder(String spanId) {
    return new SpanBuilder(spanId);
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
    private double durationMs;
    private String name;
    private String parentId;

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

    /** @return A Span object with the variables assigned to the builder class */
    public Span build() {
      return new Span(id, attributes, traceId, timestamp, serviceName, durationMs, name, parentId);
    }

    /** @return A string representing this SpanBuilder object and listing its variables */
    public String toString() {
      return "Span.SpanBuilder(id="
          + this.id
          + ", attributes="
          + this.attributes
          + ", traceId="
          + this.traceId
          + ", timestamp="
          + this.timestamp
          + ", serviceName="
          + this.serviceName
          + ", durationMs="
          + this.durationMs
          + ", name="
          + this.name
          + ", parentId="
          + this.parentId
          + ")";
    }
  }
}
