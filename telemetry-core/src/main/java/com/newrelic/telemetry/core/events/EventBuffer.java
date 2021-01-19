/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core.events;

import com.newrelic.telemetry.core.Attributes;
import com.newrelic.telemetry.core.util.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A buffer for collecting {@link Event Events}.
 *
 * <p>One instance of this class can collect many {@link Event Events}. To send them to the Events
 * API, call {@link #createBatch()} and then {@link EventBatchSender#sendBatch(EventBatch)}.
 */
public final class EventBuffer {
  private static final Logger logger = LoggerFactory.getLogger(EventBuffer.class);

  private final Queue<Event> events = new ConcurrentLinkedQueue<>();

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
   * @param event The new {@link Event} instance to be sent.
   */
  public void addEvent(Event event) {
    events.add(event);
  }

  /**
   * Get the size of the {@link Event Events} buffer.
   *
   * @return Size of the {@link Event Events} buffer.
   */
  public int size() {
    return events.size();
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
    Collection<Event> eventsForBatch = new ArrayList<>(this.events.size());

    // Drain the Event buffer and return the batch
    Event event;
    while ((event = this.events.poll()) != null) {
      eventsForBatch.add(event);
    }

    return new EventBatch(eventsForBatch, this.commonAttributes);
  }

  Queue<Event> getEvents() {
    return events;
  }

  Attributes getCommonAttributes() {
    return commonAttributes;
  }

  @Override
  public String toString() {
    return "EventBuffer{" + "events=" + events + ", commonAttributes=" + commonAttributes + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    EventBuffer that = (EventBuffer) o;

    if (getEvents() != null ? !getEvents().equals(that.getEvents()) : that.getEvents() != null)
      return false;
    return getCommonAttributes() != null
        ? getCommonAttributes().equals(that.getCommonAttributes())
        : that.getCommonAttributes() == null;
  }

  @Override
  public int hashCode() {
    int result = getEvents() != null ? getEvents().hashCode() : 0;
    result = 31 * result + (getCommonAttributes() != null ? getCommonAttributes().hashCode() : 0);
    return result;
  }
}
