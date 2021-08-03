package com.newrelic.telemetry.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.newrelic.telemetry.Attributes;
import java.util.ArrayList;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EventBufferTest {
  @Test
  @DisplayName("Get size of Events")
  void testMetricsSize() {
    long currentTimeMillis = 350;
    EventBuffer eventBuffer = new EventBuffer(new Attributes());
    Event expectedEvent =
        new Event("myEvent", new Attributes().put("key1", "val1"), currentTimeMillis);
    assertEquals(0, eventBuffer.size());
    eventBuffer.addEvent(expectedEvent);
    assertEquals(1, eventBuffer.size());
  }

  @Test
  @DisplayName("Check if a single batch is created")
  void testCreateOneBatch() {
    long currentTimeMillis = 350;
    Attributes testAttr = new Attributes();
    testAttr.put("name", "Bob");
    testAttr.put("age", 20);

    Event testEvent = new Event("testEvent", testAttr, currentTimeMillis);
    EventBuffer testEventBuffer = new EventBuffer(new Attributes());
    testEventBuffer.addEvent(testEvent);

    // ArrayList<EventBatch> testBatchesList = testEventBuffer.createBatches();
    ArrayList<EventBatch> testBatchesList = testEventBuffer.createBatch();
    assertEquals(1, testBatchesList.size());
  }

  @Test
  @DisplayName("Multiple Event Batches Not Enabled: Check if a single batch is created")
  void testCreateSingleBatchWithMultipleNotEnabled() {
    /**
     * The uncompressed payload size for this example is 292000001 bytes. If splitOnSizeLimit =
     * true, then 2 batches should be created. This is because the maximum uncompressed payload size
     * for a batch is 180000000 bytes. However, since splitOnSizeLimit = false (by default), only 1
     * batch should be created.
     */
    EventBuffer testEventBuffer = new EventBuffer(new Attributes());

    Attributes attr = new Attributes();
    attr.put("name", "Bob");
    attr.put("age", 20);
    attr.put("dept", "Business");
    attr.put("id", 123423543);

    for (int i = 0; i < 2000000; i++) {
      long timestamp = System.currentTimeMillis();
      Event testEvent = new Event("StudentDataV2", attr, timestamp);
      testEventBuffer.addEvent(testEvent);
    }

    ArrayList<EventBatch> testEventBatches = testEventBuffer.createBatch();
    assertEquals(1, testEventBatches.size());
  }

  @Test
  @DisplayName("Multiple Event Batches Enabled: Check if a single batch is created")
  void testCreateOneBatchWithMultipleEnabled() {
    long currentTimeMillis = 350;
    Attributes testAttr = new Attributes();
    testAttr.put("name", "Bob");
    testAttr.put("age", 20);

    Event testEvent = new Event("testEvent", testAttr, currentTimeMillis);
    EventBuffer testEventBuffer = new EventBuffer(new Attributes(), true);
    testEventBuffer.addEvent(testEvent);

    ArrayList<EventBatch> testBatchesList = testEventBuffer.createBatch();
    assertEquals(1, testBatchesList.size());
  }

  @Test
  @DisplayName("Multiple Event Batches Enabled: Check if multiple batches are created")
  void createMultipleBatchesWithMultipleEnabled() {
    /**
     * The uncompressed payload size for this example is 292000001 bytes. Since the maximum
     * uncompressed payload size for a batch is 180000000 bytes, 2 batches should be created.
     */
    EventBuffer testEventBuffer = new EventBuffer(new Attributes(), true);

    Attributes attr = new Attributes();
    attr.put("name", "Bob");
    attr.put("age", 20);
    attr.put("dept", "Business");
    attr.put("id", 123423543);

    for (int i = 0; i < 2000000; i++) {
      long timestamp = System.currentTimeMillis();
      Event testEvent = new Event("StudentDataV2", attr, timestamp);
      testEventBuffer.addEvent(testEvent);
    }

    ArrayList<EventBatch> testEventBatches = testEventBuffer.createBatch();
    assertEquals(2, testEventBatches.size());
  }
}
