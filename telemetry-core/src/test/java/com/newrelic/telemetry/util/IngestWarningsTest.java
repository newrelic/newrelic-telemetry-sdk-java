package com.newrelic.telemetry.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.events.Event;
import com.newrelic.telemetry.logs.Log;
import com.newrelic.telemetry.metrics.Count;
import java.util.Random;
import org.junit.jupiter.api.Test;

class IngestWarningsTest {

  @Test
  void validNumberOfAttributesForEventTest() {
    IngestWarnings testIngestWarnings = spy(new IngestWarnings());
    Attributes testAttributes = new Attributes();
    testAttributes.put("test", 1);
    testAttributes.put("name", "bob");
    testAttributes.put("sunny", true);

    long timestamp = 300;
    Event testEvent = new Event("SampleEvent", testAttributes, timestamp);

    testIngestWarnings.raiseIngestWarnings(testAttributes.asMap(), testEvent);
    verify(testIngestWarnings, never()).warningNumAttributes("Event");
  }

  @Test
  void invalidNumberOfAttributesForEventTest() {
    IngestWarnings testIngestWarnings = spy(new IngestWarnings());
    Attributes testAttributes = new Attributes();

    for (int i = 0; i < 300; i++) {
      testAttributes.put("Attribute" + i, i);
    }

    long timestamp = 300;
    Event testEvent = new Event("SampleEvent", testAttributes, timestamp);

    testIngestWarnings.raiseIngestWarnings(testAttributes.asMap(), testEvent);
    verify(testIngestWarnings).warningNumAttributes("Event");
  }

  @Test
  void validNumberOfAttributesForMetricTest() {
    IngestWarnings testIngestWarnings = spy(new IngestWarnings());
    Attributes testAttributes = new Attributes();
    testAttributes.put("test", 1);
    testAttributes.put("name", "bob");
    testAttributes.put("sunny", true);

    long testVal = 7;
    long testStartTimeMs = 100;
    long testEndTimeMs = 200;

    Count testCount =
        new Count("TestCount", testVal, testStartTimeMs, testEndTimeMs, testAttributes);

    testIngestWarnings.raiseIngestWarnings(testAttributes.asMap(), testCount);
    verify(testIngestWarnings, never()).warningNumAttributes("Metric");
  }

  @Test
  void invalidNumberOfAttributesForMetricTest() {
    IngestWarnings testIngestWarnings = spy(new IngestWarnings());
    Attributes testAttributes = new Attributes();
    for (int i = 0; i < 110; i++) {
      testAttributes.put("Attribute" + i, i);
    }

    long testVal = 7;
    long testStartTimeMs = 100;
    long testEndTimeMs = 200;

    Count testCount =
        new Count("TestCount", testVal, testStartTimeMs, testEndTimeMs, testAttributes);

    testIngestWarnings.raiseIngestWarnings(testAttributes.asMap(), testCount);
    verify(testIngestWarnings).warningNumAttributes("Metric");
  }

  @Test
  void validNumberOfAttributesForLogTest() {
    IngestWarnings testIngestWarnings = spy(new IngestWarnings());
    Attributes testAttributes = new Attributes();
    testAttributes.put("test", 1);
    testAttributes.put("name", "bob");
    testAttributes.put("sunny", true);

    Log testLog =
        Log.builder()
            .attributes(testAttributes)
            .message("Processing Information")
            .level("DEBUG")
            .build();

    testIngestWarnings.raiseIngestWarnings(testAttributes.asMap(), testLog);
    verify(testIngestWarnings, never()).warningNumAttributes("Log");
  }

  @Test
  void invalidNumberOfAttributesForLogTest() {
    IngestWarnings testIngestWarnings = spy(new IngestWarnings());
    Attributes testAttributes = new Attributes();

    for (int i = 0; i < 300; i++) {
      testAttributes.put("Attribute" + i, i);
    }

    Log testLog =
        Log.builder()
            .attributes(testAttributes)
            .message("Processing Information")
            .level("DEBUG")
            .build();

    testIngestWarnings.raiseIngestWarnings(testAttributes.asMap(), testLog);
    verify(testIngestWarnings).warningNumAttributes("Log");
  }

  // add # of attributes test(s) for logs

  @Test
  void validAttributeNamesTest() {
    IngestWarnings testIngestWarnings = spy(new IngestWarnings());

    Attributes testAttributes = new Attributes();
    testAttributes.put("test", 1);
    testAttributes.put("name", "bob");
    testAttributes.put("sunny", true);

    long timestamp = 300;
    Event testEvent = new Event("SampleEvent", testAttributes, timestamp);

    testIngestWarnings.raiseIngestWarnings(testAttributes.asMap(), testEvent);
    verify(testIngestWarnings, never()).attributeNameWarning("test");
  }

  @Test
  void invalidAttributeNamesTest() {

    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    StringBuilder sb = new StringBuilder();
    Random random = new Random();
    int length = 300;

    for (int i = 0; i < length; i++) {
      int index = random.nextInt(alphabet.length());
      char randomChar = alphabet.charAt(index);
      sb.append(randomChar);
    }

    String longAttrName = sb.toString();

    IngestWarnings testIngestWarnings = spy(new IngestWarnings());

    Attributes testAttributes = new Attributes();
    testAttributes.put(longAttrName, 1);
    testAttributes.put("name", "bob");
    testAttributes.put("sunny", true);

    long timestamp = 300;
    Event testEvent = new Event("SampleEvent", testAttributes, timestamp);

    testIngestWarnings.raiseIngestWarnings(testAttributes.asMap(), testEvent);
    verify(testIngestWarnings).attributeNameWarning(longAttrName);
  }

  @Test
  void validAttributeValuesTest() {
    IngestWarnings testIngestWarnings = spy(new IngestWarnings());

    Attributes testAttributes = new Attributes();
    testAttributes.put("test", 1);
    testAttributes.put("name", "bob");
    testAttributes.put("sunny", true);

    long timestamp = 300;
    Event testEvent = new Event("SampleEvent", testAttributes, timestamp);

    testIngestWarnings.raiseIngestWarnings(testAttributes.asMap(), testEvent);
    verify(testIngestWarnings, never()).attributeValueWarning("test");
  }

  @Test
  void invalidAttributeValuesTest() {
    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    StringBuilder sb = new StringBuilder();
    Random random = new Random();
    int length = 5000;

    for (int i = 0; i < length; i++) {
      int index = random.nextInt(alphabet.length());
      char randomChar = alphabet.charAt(index);
      sb.append(randomChar);
    }

    String longAttrValue = sb.toString();

    IngestWarnings testIngestWarnings = spy(new IngestWarnings());

    Attributes testAttributes = new Attributes();
    testAttributes.put("test", longAttrValue);
    testAttributes.put("name", "bob");
    testAttributes.put("sunny", true);

    long timestamp = 300;
    Event testEvent = new Event("SampleEvent", testAttributes, timestamp);

    testIngestWarnings.raiseIngestWarnings(testAttributes.asMap(), testEvent);

    verify(testIngestWarnings).attributeValueWarning(longAttrValue);
  }

  @Test
  void invalidAttributeNameAndValueTest() {
    String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    StringBuilder sb = new StringBuilder();
    Random random = new Random();
    int length = 5000;

    for (int i = 0; i < length; i++) {
      int index = random.nextInt(alphabet.length());
      char randomChar = alphabet.charAt(index);
      sb.append(randomChar);
    }

    String longAttrValue = sb.toString();

    IngestWarnings testIngestWarnings = spy(new IngestWarnings());

    Attributes testAttributes = new Attributes();
    testAttributes.put(longAttrValue, longAttrValue);
    testAttributes.put("name", "bob");
    testAttributes.put("sunny", true);

    long timestamp = 300;
    Event testEvent = new Event("SampleEvent", testAttributes, timestamp);

    testIngestWarnings.raiseIngestWarnings(testAttributes.asMap(), testEvent);

    verify(testIngestWarnings).attributeNameWarning(longAttrValue);
    verify(testIngestWarnings).attributeValueWarning(longAttrValue);
  }
}
