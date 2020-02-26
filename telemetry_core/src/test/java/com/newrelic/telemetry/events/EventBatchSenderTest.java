package com.newrelic.telemetry.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Response;
import java.util.Collections;
import org.junit.jupiter.api.Test;

public class EventBatchSenderTest {

  @Test
  void testSimpleSend() throws Exception {
    Event el;
  }

  @Test
  void testEmptyBatch() throws Exception {
    EventBatchSender testClass = new EventBatchSender(null, null);
    EventBatch batch = new EventBatch(Collections.emptyList(), new Attributes());
    Response response = testClass.sendBatch(batch);
    assertEquals(202, response.getStatusCode());
  }
}
