package com.newrelic.telemetry.util;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestWarnings {
  private static final Logger logger = LoggerFactory.getLogger(IngestWarnings.class);
  private static final int MAX_NUMBER_OF_EVENT_ATTRIBUTES =
      254; // Public documentation says 255, but true maximum is 254
  private static final int MAX_NUMBER_OF_METRIC_ATTRIBUTES = 100;
  private static final int MAX_NUMBER_OF_LOG_ATTRIBUTES =
      254; // Public documentation says 255, but true maximum is 254

  private static final int MAX_ATTRIBUTE_NAME_LENGTH = 255;
  private static final int MAX_ATTRIBUTE_VALUE_LENGTH = 4096;

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

  public void attributeNameWarning(String attributeName) {
    logger.warn(
        "The length of the attribute named "
            + attributeName
            + " is greater than the maximum length allowed for an attribute name.");
  }

  public void attributeValueWarning(String attributeValue) {
    logger.warn(
        "The value of the attribute "
            + attributeValue
            + " is greater than the maximum length allowed for an attribute value.");
  }

  public void raiseIngestWarnings(Map<String, Object> attributes, String dataType) {

    // First Check - Check for valid number of attributes

    int numberOfAttributes = attributes.size();
    if (dataType.equals("Event")) {
      if (numberOfAttributes > MAX_NUMBER_OF_EVENT_ATTRIBUTES) {
        eventWarningNumAttributes();
      }
    }
    if (dataType.equals("Metric")) {
      if (numberOfAttributes > MAX_NUMBER_OF_METRIC_ATTRIBUTES) {
        metricWarningNumAttributes();
      }
    }
    if (dataType.equals("Log")) {
      if (numberOfAttributes > MAX_NUMBER_OF_LOG_ATTRIBUTES) {
        logWarningNumAttributes();
      }
    }

    // Second Check - Check that the attribute names are valid

    for (String attributeName : attributes.keySet()) {
      if (attributeName != null) {
        if (attributeName.length() > MAX_ATTRIBUTE_NAME_LENGTH) {
          attributeNameWarning(attributeName);
        }
      }
    }

    // Third Check - Check that the attribute values are valid

    for (String attributeName : attributes.keySet()) {
      if (attributeName != null) {
        if (attributes.get(attributeName) instanceof String) {
          String attributeValue = attributes.get(attributeName).toString();
          if (attributeValue.length() > MAX_ATTRIBUTE_VALUE_LENGTH) {
            attributeValueWarning(attributeValue);
          }
        }
      }
    }
  }
}
