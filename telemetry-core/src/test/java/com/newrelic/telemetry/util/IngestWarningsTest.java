package com.newrelic.telemetry.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.junit.jupiter.api.Test;

class IngestWarningsTest {

  @Test
  void validNumberOfAttributesForEventTest() {
    IngestWarnings testIngestWarnings = spy(new IngestWarnings());
    Map<String, Object> testAttributes = new HashMap<>();
    testAttributes.put("test", 1);
    testAttributes.put("name", "bob");
    testAttributes.put("sunny", true);

    testIngestWarnings.isValidNumberOfAttributes(testAttributes, "Event");
    verify(testIngestWarnings, never()).eventWarningNumAttributes();
  }

  @Test
  void invalidNumberOfAttributesForEventTest() {
    IngestWarnings testIngestWarnings = spy(new IngestWarnings());
    Map<String, Object> testAttributes = new HashMap<>();

    for (int i = 0; i < 300; i++) {
      testAttributes.put("Attribute" + i, i);
    }

    testIngestWarnings.isValidNumberOfAttributes(testAttributes, "Event");
    verify(testIngestWarnings).eventWarningNumAttributes();
  }

  @Test
  void validNumberOfAttributesForMetricTest() {
    IngestWarnings testIngestWarnings = spy(new IngestWarnings());
    Map<String, Object> testAttributes = new HashMap<>();
    testAttributes.put("test", 1);
    testAttributes.put("name", "bob");
    testAttributes.put("sunny", true);

    testIngestWarnings.isValidNumberOfAttributes(testAttributes, "Metric");
    verify(testIngestWarnings, never()).metricWarningNumAttributes();
  }

  @Test
  void invalidNumberOfAttributesForMetricTest() {
    IngestWarnings testIngestWarnings = spy(new IngestWarnings());
    Map<String, Object> testAttributes = new HashMap<>();
    for (int i = 0; i < 110; i++) {
      testAttributes.put("Attribute" + i, i);
    }

    testIngestWarnings.isValidNumberOfAttributes(testAttributes, "Metric");
    verify(testIngestWarnings).metricWarningNumAttributes();
  }

  @Test
  void validNumberOfAttributesForLogTest() {
    IngestWarnings testIngestWarnings = spy(new IngestWarnings());
    Map<String, Object> testAttributes = new HashMap<>();
    testAttributes.put("test", 1);
    testAttributes.put("name", "bob");
    testAttributes.put("sunny", true);

    testIngestWarnings.isValidNumberOfAttributes(testAttributes, "Log");
    verify(testIngestWarnings, never()).logWarningNumAttributes();
  }

  @Test
  void invalidNumberOfAttributesForLogTest() {
    IngestWarnings testIngestWarnings = spy(new IngestWarnings());
    Map<String, Object> testAttributes = new HashMap<>();

    for (int i = 0; i < 300; i++) {
      testAttributes.put("Attribute" + i, i);
    }

    testIngestWarnings.isValidNumberOfAttributes(testAttributes, "Log");
    verify(testIngestWarnings).logWarningNumAttributes();
  }

  // add # of attributes test(s) for logs

  @Test
  void validAttributeNamesTest() {
    IngestWarnings testIngestWarnings = spy(new IngestWarnings());
    Map<String, Object> testAttributes = new HashMap<>();
    testAttributes.put("test", 1);
    testAttributes.put("name", "bob");
    testAttributes.put("sunny", true);

    testIngestWarnings.validAttributeNames(testAttributes);
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
    Map<String, Object> testAttributes = new HashMap<>();
    testAttributes.put(longAttrName, 1);
    testAttributes.put("name", "bob");
    testAttributes.put("sunny", true);

    testIngestWarnings.validAttributeNames(testAttributes);
    verify(testIngestWarnings).attributeNameWarning(longAttrName);
  }

  @Test
  void validAttributeValuesTest() {

    IngestWarnings testIngestWarnings = spy(new IngestWarnings());
    Map<String, Object> testAttributes = new HashMap<>();
    testAttributes.put("test", 1);
    testAttributes.put("name", "bob");
    testAttributes.put("sunny", true);

    testIngestWarnings.validAttributeValues(testAttributes);
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
    Map<String, Object> testAttributes = new HashMap<>();
    testAttributes.put("test", longAttrValue);
    testAttributes.put("name", "bob");
    testAttributes.put("sunny", true);

    testIngestWarnings.validAttributeValues(testAttributes);
    verify(testIngestWarnings).attributeValueWarning(longAttrValue);
  }
}
