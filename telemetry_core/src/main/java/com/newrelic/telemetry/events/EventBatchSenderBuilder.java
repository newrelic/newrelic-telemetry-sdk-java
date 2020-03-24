package com.newrelic.telemetry.events;

import com.newrelic.telemetry.AbstractSenderBuilder;
import com.newrelic.telemetry.events.json.EventBatchJsonCommonBlockWriter;
import com.newrelic.telemetry.events.json.EventBatchJsonTelemetryBlockWriter;
import com.newrelic.telemetry.events.json.EventBatchMarshaller;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.transport.BatchDataSender;
import com.newrelic.telemetry.util.Utils;
import java.net.URL;

public class EventBatchSenderBuilder extends AbstractSenderBuilder<EventBatchSenderBuilder> {

  private static final String eventsPath = "/metric/v1";
  private static final String DEFAULT_URL = "https://trace-api.newrelic.com/";

  public EventBatchSender build() {
    Utils.verifyNonNull(apiKey, "API key cannot be null");
    Utils.verifyNonNull(httpPoster, "an HttpPoster implementation is required.");

    URL url = getOrDefaultSendUrl();

    EventBatchMarshaller marshaller =
        new EventBatchMarshaller(
            new EventBatchJsonCommonBlockWriter(new AttributesJson()),
            new EventBatchJsonTelemetryBlockWriter());

    BatchDataSender sender =
        new BatchDataSender(httpPoster, apiKey, url, auditLoggingEnabled, secondaryUserAgent);

    return new EventBatchSender(marshaller, sender);
  }

  @Override
  protected String getDefaultUrl() {
    return DEFAULT_URL;
  }

  @Override
  protected String getBasePath() {
    return eventsPath;
  }
}
