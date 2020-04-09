package com.newrelic.telemetry.events.json;

import com.newrelic.telemetry.events.Event;
import com.newrelic.telemetry.events.EventBatch;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EventBatchJsonTelemetryBlockWriter {

  public void appendTelemetryJson(EventBatch batch, StringBuilder builder) {
    //    builder.append("\"metrics\":").append("[");
    Collection<Event> metrics = batch.getTelemetry();

    Function<Event, Event> decorator = Function.identity();
    if (batch.hasCommonAttributes()) {
      decorator =
          e -> {
            e.getAttributes().putAll(batch.getCommonAttributes());
            return e;
          };
    }

    builder.append(
        metrics.stream().map(decorator).map(new EventToJson()).collect(Collectors.joining(",")));

    //    builder.append("]");
  }
}
