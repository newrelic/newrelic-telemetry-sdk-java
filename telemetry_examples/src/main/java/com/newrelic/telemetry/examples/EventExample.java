package com.newrelic.telemetry.examples;

import com.newrelic.telemetry.core.Attributes;
import com.newrelic.telemetry.core.EventBatchSenderFactory;
import com.newrelic.telemetry.core.events.Event;
import com.newrelic.telemetry.core.events.EventBatchSender;
import com.newrelic.telemetry.core.events.EventBuffer;
import com.newrelic.telemetry.okhttp.OkHttpPoster;

public class EventExample {
  public static void main(String[] args) throws Exception {
    String insightsInsertKey = args[0];

    EventBatchSenderFactory factory =
        EventBatchSenderFactory.fromHttpImplementation(OkHttpPoster::new);
    EventBatchSender sender =
        EventBatchSender.create(factory.configureWith(insightsInsertKey).build());
    EventBuffer eventBuffer = new EventBuffer(getCommonAttributes());

    Attributes attr = new Attributes();
    attr.put("foo", 1234);
    attr.put("bar", "baz");
    attr.put("quux", true);

    long timestamp = System.currentTimeMillis();
    Event event = new Event("SampleEvent", attr, timestamp);
    eventBuffer.addEvent(event);

    sender.sendBatch(eventBuffer.createBatch());
  }

  private static Attributes getCommonAttributes() {
    return new Attributes().put("exampleName", "CountExample");
  }
}
