package com.newrelic.telemetry.spans;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Telemetry;
import lombok.Value;

/** TODO: Document me */
@Value
public final class Span implements Telemetry {

  private final String id;
  private final Attributes attributes;

  private final String traceId;      // trace.id <- top level
  private final long timestamp;      // in epoch ms

  private final String serviceName;  // service.name <- goes in attributes
  private final double durationMs;   // duration.ms <- goes in attributes
  private final String name;         // goes in attributes
  private final String parentId;     // parent.id <- goes in attributes

  private Span(String id, Attributes attributes, String traceId, long timestamp, String serviceName,
      double durationMs, String name, String parentId) {
    this.id = id;
    this.attributes = attributes;
    this.traceId = traceId;
    this.timestamp = timestamp;
    this.serviceName = serviceName;
    this.durationMs = durationMs;
    this.name = name;
    this.parentId = parentId;
  }

  public static SpanBuilder builder(String spanId) {
    return new SpanBuilder(spanId);
  }

  @Override
  public Type getType() {
    return Type.SPAN;
  }


  public static class SpanBuilder {

    private String id;
    private Attributes attributes = new Attributes();
    private String traceId;
    private long timestamp = System.currentTimeMillis();
    private String serviceName;
    private double durationMs;
    private String name;
    private String parentId;

    SpanBuilder(String spanId) {
      this.id = spanId;
    }

    public SpanBuilder attributes(Attributes attributes) {
      this.attributes = attributes;
      return this;
    }

    public SpanBuilder traceId(String traceId) {
      this.traceId = traceId;
      return this;
    }

    public SpanBuilder timestamp(long timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public SpanBuilder serviceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    public SpanBuilder durationMs(double durationMs) {
      this.durationMs = durationMs;
      return this;
    }

    public SpanBuilder name(String name) {
      this.name = name;
      return this;
    }

    public SpanBuilder parentId(String parentId) {
      this.parentId = parentId;
      return this;
    }

    public Span build() {
      return new Span(id, attributes, traceId, timestamp, serviceName, durationMs, name, parentId);
    }

    public String toString() {
      return "Span.SpanBuilder(id=" + this.id + ", attributes=" + this.attributes + ", traceId="
          + this.traceId + ", timestamp=" + this.timestamp + ", serviceName=" + this.serviceName
          + ", durationMs=" + this.durationMs + ", name=" + this.name + ", parentId="
          + this.parentId
          + ")";
    }
  }
}
