package com.newrelic.telemetry.events.json;

import com.newrelic.telemetry.events.Event;
import com.newrelic.telemetry.events.EventBatch;
import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBatchMarshaller {

  private static final Logger logger = LoggerFactory.getLogger(EventBatchMarshaller.class);

  public EventBatchMarshaller(
      EventBatchJsonCommonBlockWriter commonBlockWriter,
      EventBatchJsonTelemetryBlockWriter telemetryBlockWriter) {}

  public String toJson(EventBatch batch) {
    logger.debug("Generating json for event batch.");
    StringBuilder builder = new StringBuilder();

    builder.append("[");
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

    builder.append("]");
    return builder.toString();
  }
}
