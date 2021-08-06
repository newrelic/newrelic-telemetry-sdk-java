/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.events;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.events.json.EventBatchMarshaller;
import com.newrelic.telemetry.util.IngestWarnings;
import com.newrelic.telemetry.util.Utils;
import java.nio.charset.StandardCharsets;
import java.util.*;
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
  private final IngestWarnings ingestWarnings = new IngestWarnings();
  private final Attributes commonAttributes;
  private final boolean splitBatch;

  /**
   * Create a new buffer with the provided common set of attributes.
   *
   * @param commonAttributes These attributes will be appended (by the New Relic backend) to every
   *     {@link Event} in this buffer.
   */
  public EventBuffer(Attributes commonAttributes) {
    this(commonAttributes, false);
  }

  /**
   * Create a new buffer with the provided common set of attributes.
   *
   * @param commonAttributes These attributes will be appended (by the New Relic backend) to every
   *     {@link Event} in this buffer.
   * @param splitOnSizeLimit Flag to indicate whether to split batch when size limit is hit.
   */
  public EventBuffer(Attributes commonAttributes, boolean splitOnSizeLimit) {
    this.commonAttributes = Utils.verifyNonNull(commonAttributes);
    this.splitBatch = splitOnSizeLimit;
  }

  /**
   * Append a {@link Event} to this buffer, to be sent in the next {@link EventBatch}.
   *
   * @param event The new {@link Event} instance to be sent.
   */
  public void addEvent(Event event) {
    Map<String, Object> attributes = event.getAttributes().asMap();
    ingestWarnings.raiseIngestWarnings(attributes, event);
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
  public EventBatch createSingleBatch() {
    logger.debug("Creating Event batch.");
    Collection<Event> eventsForBatch = new ArrayList<>(this.events.size());

    // Drain the Event buffer and return the batch
    Event event;
    while ((event = this.events.poll()) != null) {
      eventsForBatch.add(event);
    }
    return new EventBatch(eventsForBatch, this.commonAttributes);
  }

  /**
   * Creates an ArrayList of EventBatch objects from the contents of this buffer, then clears the
   * contents of this buffer.
   *
   * <p>{@link Event Events} are added to an EventBatch. When each event is added, the size (in
   * bytes) of the event is calculated. When the total size of the events in the batch exceeds the
   * MAX_UNCOMPRESSED_BATCH_SIZE, the current batch is sent to New Relic, and a new EventBatch is
   * created. This process repeats until all events are removed from the queue.
   *
   * @return An ArrayList of EventBatch objects. Each {@link EventBatch} in the ArrayList contains
   *     an immutable collection of {@link Event Events}.
   */
  public ArrayList<EventBatch> createBatches() {
    logger.debug("Creating Event batch.");

    int currentUncompressedBatchSize = 0;
    int MAX_UNCOMPRESSED_BATCH_SIZE = 180000000;

    ArrayList<EventBatch> batches = new ArrayList<>();
    Collection<Event> eventsForBatch = new ArrayList<>();

    // Drain the Event buffer and return the batch
    Event event;

    while ((event = this.events.poll()) != null) {
      String partialEventJson = EventBatchMarshaller.mapToJson(event);

      // Insert common attributes into event JSON

      Map<String, Object> attrs = getCommonAttributes().asMap();
      String fullEventJson = partialEventJson.substring(0, partialEventJson.length() - 1);
      Set<String> keys = getCommonAttributes().asMap().keySet();
      for (String key : keys) {
        String quoteName = "," + '"' + key + '"' + ':' + '"' + attrs.get(key) + '"';
        fullEventJson += quoteName;
      }
      fullEventJson += "}";

      // Calculate size of event JSON (in bytes) and add it to the currentUncompressedBatchSize

      currentUncompressedBatchSize += (fullEventJson.getBytes(StandardCharsets.UTF_8).length);

      if (currentUncompressedBatchSize > MAX_UNCOMPRESSED_BATCH_SIZE) {
        EventBatch e = new EventBatch(eventsForBatch, this.commonAttributes);
        batches.add(e);
        eventsForBatch = new ArrayList<>();
        currentUncompressedBatchSize = fullEventJson.getBytes(StandardCharsets.UTF_8).length;
      }
      eventsForBatch.add(event);
    }

    batches.add(new EventBatch(eventsForBatch, this.commonAttributes));
    return batches;
  }

  /**
   * Creates an ArrayList of EventBatch objects by calling {@link #createSingleBatch()} or {@link
   * #createBatches()}. This depends on if the user wants to split batches on size limit or not
   * (splitBatch). If splitBatch = false, {@link #createSingleBatch()} is called. If splitBatch =
   * true, {@link #createBatches()} is called.
   *
   * @return An ArrayList of EventBatch objects. Each {@link EventBatch} in the ArrayList contains
   *     an immutable collection of {@link Event Events}.
   */
  public ArrayList<EventBatch> createBatch() {
    ArrayList<EventBatch> batches = new ArrayList<EventBatch>();
    if (splitBatch == false) {
      EventBatch singleEventBatch = createSingleBatch();
      batches.add(singleEventBatch);
      return batches;
    } else {
      batches = createBatches();
      return batches;
    }
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
