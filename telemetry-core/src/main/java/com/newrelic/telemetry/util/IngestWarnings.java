package com.newrelic.telemetry.util;

import com.newrelic.telemetry.Telemetry;
import com.newrelic.telemetry.events.Event;
import com.newrelic.telemetry.logs.Log;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestWarnings {
  private static final Logger logger = LoggerFactory.getLogger(IngestWarnings.class);
  private static final int MAX_NUMBER_OF_EVENT_ATTRIBUTES =
      254; // Public documentation says 255, but true maximum is 254. This is referenced in the
  // following Slack thread: https://newrelic.slack.com/archives/CAHBUB7A9/p1625002090399400.
  private static final int MAX_NUMBER_OF_METRIC_ATTRIBUTES = 100;
  private static final int MAX_NUMBER_OF_LOG_ATTRIBUTES =
      254; // Public documentation says 255, but true maximum is 254. This is reference in the
  // following Slack thread: https://newrelic.slack.com/archives/CAHBUB7A9/p1625002090399400

  private static final int MAX_ATTRIBUTE_NAME_LENGTH = 255;
  private static final int MAX_ATTRIBUTE_VALUE_LENGTH = 4096;

  public void raiseIngestWarnings(Map<String, Object> attributes, Telemetry dataType) {

    // First Check - Check for valid number of attributes

    int numberOfAttributes = attributes.size();
    if (dataType instanceof Event) {
      if (numberOfAttributes > MAX_NUMBER_OF_EVENT_ATTRIBUTES) {
        warningNumAttributes("Event");
      }
    }
    if (dataType instanceof Metric) {
      if (numberOfAttributes > MAX_NUMBER_OF_METRIC_ATTRIBUTES) {
        warningNumAttributes("Metric");
      }
    }
    if (dataType instanceof Log) {
      if (numberOfAttributes > MAX_NUMBER_OF_LOG_ATTRIBUTES) {
        warningNumAttributes("Log");
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

  public void warningNumAttributes(String telemetryType) {
    logger.warn(
        "The number of attributes in this {} is greater than the maximum allowed attributes per {}.",
        telemetryType,
        telemetryType);
  }

  public void attributeNameWarning(String attributeName) {
    logger.warn(
        "The length of the attribute named {} is greater than the maximum length allowed for an attribute name.",
        attributeName);
  }

  public void attributeValueWarning(String attributeValue) {
    logger.warn(
        "The value of the attribute, {}, is greater than the maximum length allowed for an attribute value.",
        attributeValue);
  }
}
