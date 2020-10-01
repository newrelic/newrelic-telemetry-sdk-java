/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.telemetry.events.json;

import com.newrelic.telemetry.events.Event;
import com.newrelic.telemetry.events.EventBatch;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBatchMarshaller {

  private static final Logger logger = LoggerFactory.getLogger(EventBatchMarshaller.class);

  public String toJson(EventBatch batch) {
    logger.debug("Generating json for event batch.");

    Function<Event, Event> decorator = Function.identity();
    if (batch.hasCommonAttributes()) {
      decorator =
          event -> {
            Event out = new Event(event);
            out.getAttributes().putAll(batch.getCommonAttributes());
            return out;
          };
    }

    StringBuilder builder = new StringBuilder();
    builder.append("[");
    builder.append(
        batch
            .getTelemetry()
            .stream()
            .map(decorator)
            .map(new EventToJson())
            .collect(Collectors.joining(",")));
    builder.append("]");
    return builder.toString();
  }
}
