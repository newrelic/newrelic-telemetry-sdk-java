package com.newrelic.telemetry.events;

import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.TelemetryBatch;
import com.newrelic.telemetry.events.json.EventBatchMarshaller;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
import com.newrelic.telemetry.transport.BatchDataSender;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
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
   * Send a batch of events to New Relic.
   *
   * @param batch The batch to send. This batch will be drained of accumulated events as a part of
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
        "Sending an event batch (number of events: {}) to the New Relic event ingest endpoint)",
        batch.size());
    String json = marshaller.toJson(batch);

    try {
      return sender.send(json);
    } catch (RetryWithSplitException splitx) {
      return splitBatchAndSend(batch, new LinkedBlockingDeque<>());
    }
  }

  @SuppressWarnings("unchecked")
  Response splitBatchAndSend(EventBatch batch, BlockingDeque<TelemetryBatch> queue) {
    logger.info(
        "Tried to send a too-large event batch (number of events: {}) to the New Relic event ingest endpoint. Splitting)",
        batch.size());
    List<TelemetryBatch<Event>> twoBatches = batch.split();
    Response response = null;

    // Preserve in-order processing
    queue.addFirst(twoBatches.get(1));
    queue.addFirst(twoBatches.get(0));
    while (!queue.isEmpty()) {
      final EventBatch eb = (EventBatch) queue.pollFirst();
      final String json = marshaller.toJson(eb);

      try {
        response = sender.send(json);
      } catch (RetryWithSplitException splitx) {
        response = splitBatchAndSend(eb, queue);
      } catch (ResponseException rsx) {
        // We have to log and swallow this exception as there may be other split batches
        // might succeed at sending
        logger.info(
            "Failed to send a split event batch to the New Relic event ingest endpoint. Exception: {}",
            rsx);
      }
    }
    if (response == null) {
      response =
          new Response(
              200, "OK", "Large payload was split - check log in case of dropped sub-batch");
    }

    return response;
  }
}
