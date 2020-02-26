/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.events;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.util.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.slf4j.LoggerFactory;

/**
 * A buffer for collecting {@link Event Events}.
 *
 * <p>One instance of this class can collect many {@link Event Events}. To send them to the Events
 * API, call {@link #createBatch()} and then {@link EventBatchSender#sendBatch(EventBatch)}.
 *
 * <p>This class is thread-safe.
 */
@Value
public class EventBuffer {
  private static final org.slf4j.Logger logger = LoggerFactory.getLogger(EventBuffer.class);

  @Getter(AccessLevel.PACKAGE)
  private final Queue<Event> Events = new ConcurrentLinkedQueue<>();

  @Getter(AccessLevel.PACKAGE)
  private final Attributes commonAttributes;

  /**
   * Create a new buffer with the provided common set of attributes.
   *
   * @param commonAttributes These attributes will be appended (by the New Relic backend) to every
   *     {@link Event} in this buffer.
   */
  public EventBuffer(Attributes commonAttributes) {
    this.commonAttributes = Utils.verifyNonNull(commonAttributes);
  }

  /**
   * Append a {@link Event} to this buffer, to be sent in the next {@link EventBatch}.
   *
   * @param Event The new {@link Event} instance to be sent.
   */
  public void addEvent(Event Event) {
    Events.add(Event);
  }

  /**
   * Creates a new {@link EventBatch} from the contents of this buffer, then clears the contents of
   * this buffer.
   *
   * <p>{@link Event Events} added to this buffer by other threads during this method call will
   * either be added to the {@link EventBatch} being created, or will be saved for the next {@link
   * EventBatch}.
   *
   * @return A new {@link EventBatch} with an immutable collection of {@link Event Events}.
   */
  public EventBatch createBatch() {
    logger.debug("Creating Event batch.");
    Collection<Event> Events = new ArrayList<>(this.Events.size());

    // Drain the Event buffer and return the batch
    Event Event;
    while ((Event = this.Events.poll()) != null) {
      Events.add(Event);
    }

    return new EventBatch(Events, this.commonAttributes);
  }
}
