package com.newrelic.telemetry.util;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestWarnings {
  private static final Logger logger = LoggerFactory.getLogger(IngestWarnings.class);
  private static final int maxNumberOfEventAttributes = 254;
  private static final int maxNumberOfMetricAttributes = 100;
  private static final int maxNumberOfLogAttributes = 254;

  private static final int maxAttributeNameLength = 255;
  private static final int maxAttributeValueLength = 4096;

  private static final String validEventTypeRegex = "^[a-zA-Z0-9_:]";

  public void eventWarningNumAttributes() {
    logger.warn(
        "The number of attributes in this event is greater than the maximum allowed attributes per event.");
  }

  public void metricWarningNumAttributes() {
    logger.warn(
        "The number of attributes in this metric is greater than the maximum allowed attributes per metric.");
  }

  public void logWarningNumAttributes() {
    logger.warn(
        "The number of attributes in this log is greater than the maximum allowed attributes per log.");
  }

  public void isValidNumberOfAttributes(Map<String, Object> attributes, String dataType) {
    int numberOfAttributes = attributes.size();
    if (dataType.equals("Event")) {
      if (numberOfAttributes > maxNumberOfEventAttributes) {
        eventWarningNumAttributes();
      }
    }
    if (dataType.equals("Metric")) {
      if (numberOfAttributes > maxNumberOfMetricAttributes) {
        metricWarningNumAttributes();
      }
    }
    if (dataType.equals("Log")) {
      if (numberOfAttributes > maxNumberOfLogAttributes) {
        logWarningNumAttributes();
      }
    }
  }

  public void attributeNameWarning(String attributeName) {
    logger.warn(
        "The length of the attribute named "
            + attributeName
            + " is greater than the maximum length allowed for an attribute name.");
  }

  public void validAttributeNames(Map<String, Object> attributes) {
    for (String attributeName : attributes.keySet()) {
      if (attributeName.length() > maxAttributeNameLength) {
        attributeNameWarning(attributeName);
      }
    }
  }

  public void attributeValueWarning(String attributeValue) {
    logger.warn(
        "The value of the attribute "
            + attributeValue
            + " is greater than the maximum length allowed for an attribute value.");
  }

  public void validAttributeValues(Map<String, Object> attributes) {
    for (String attributeName : attributes.keySet()) {
      if (attributes.get(attributeName) instanceof String) {
        String attributeValue = attributes.get(attributeName).toString();
        if (attributeValue.length() > maxAttributeValueLength) {
          attributeValueWarning(attributeValue);
        }
      }
    }
  }
}
