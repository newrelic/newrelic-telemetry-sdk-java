package com.newrelic.telemetry.events.json;

import com.newrelic.telemetry.events.Event;
import com.newrelic.telemetry.events.EventBatch;
import java.util.Collection;
import java.util.stream.Collectors;

public class EventBatchJsonTelemetryBlockWriter {

  public void appendTelemetryJson(EventBatch batch, StringBuilder builder) {
    builder.append("\"metrics\":").append("[");
    Collection<Event> metrics = batch.getTelemetry();

    builder.append(metrics.stream().map(new EventToJson()).collect(Collectors.joining(",")));

    builder.append("]");
  }
}