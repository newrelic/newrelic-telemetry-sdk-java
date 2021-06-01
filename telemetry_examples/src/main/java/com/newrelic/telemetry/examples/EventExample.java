package com.newrelic.telemetry.examples;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.EventBatchSenderFactory;
import com.newrelic.telemetry.OkHttpPoster;
import com.newrelic.telemetry.events.Event;
import com.newrelic.telemetry.events.EventBatchSender;
import com.newrelic.telemetry.events.EventBuffer;

/**
 * This is an example of sending an Event to New Relic.
 *
 * <p>An EventBatchSender is created by configuring an EventBatchSenderFactory object with a License
 * Key. Then, an event is created with an EventType, Attributes, and the current time in
 * milliseconds (UTC time). The event is added to an EventBuffer and sent via sender.sendBatch().
 *
 * <p>To run this example, provide your License Key.
 */
public class EventExample {
  public static void main(String[] args) throws Exception {

    String licenseKey = args[0];
    EventBatchSenderFactory factory =
        EventBatchSenderFactory.fromHttpImplementation(OkHttpPoster::new);

    EventBatchSender sender =
        EventBatchSender.create(factory.configureWith(licenseKey).useLicenseKey(true).build());

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
