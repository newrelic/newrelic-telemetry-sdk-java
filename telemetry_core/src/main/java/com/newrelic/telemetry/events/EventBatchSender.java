package com.newrelic.telemetry.events;

import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.events.json.EventBatchMarshaller;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.transport.BatchDataSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventBatchSender {

  private static final Logger logger = LoggerFactory.getLogger(EventBatchSender.class);

  private final EventBatchMarshaller marshaller;
  private final BatchDataSender sender;

  public EventBatchSender(EventBatchMarshaller marshaller, BatchDataSender sender) {
    this.marshaller = marshaller;
    this.sender = sender;
  }

  public static EventBatchSenderBuilder builder() {
    return new EventBatchSenderBuilder();
  }

  /**
   * Send a batch of metrics to New Relic.
   *
   * @param batch The batch to send. This batch will be drained of accumulated metrics as a part of
   *     this process.
   * @return The response from the ingest API.
   * @throws ResponseException In cases where the batch is unable to be successfully sent, one of
   *     the subclasses of {@link ResponseException} will be thrown. See the documentation on that
   *     hierarchy for details on the recommended ways to respond to those exceptions.
   */
  public Response sendBatch(EventBatch batch) throws ResponseException {
    if (batch == null || batch.size() == 0) {
      logger.debug("Skipped sending of an empty event batch.");
      return new Response(202, "Ignored", "Empty batch");
    }
    logger.debug(
        "Sending an event batch (number of metrics: {}) to the New Relic event ingest endpoint)",
        batch.size());
    String json = marshaller.toJson(batch);
    return sender.send(json);
  }
}
