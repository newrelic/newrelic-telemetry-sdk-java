package com.newrelic.telemetry.spans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.spans.json.SpanBatchMarshaller;
import com.newrelic.telemetry.transport.BatchDataSender;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SpanBatchSenderTest {

  @Test
  void testSimpleSend() throws Exception {
    Span span = Span.builder("123").build();
    SpanBatch batch =
        new SpanBatch(Collections.singletonList(span), new Attributes().put("j", "k"));
    String json = "{a great document}";
    Response response = new Response(123, "OK", "yup");

    SpanBatchMarshaller marshaller = mock(SpanBatchMarshaller.class);
    BatchDataSender sender = mock(BatchDataSender.class);

    when(marshaller.toJson(batch)).thenReturn(json);
    when(sender.send(json)).thenReturn(response);

    SpanBatchSender testClass = new SpanBatchSender(marshaller, sender);

    Response result = testClass.sendBatch(batch);
    assertEquals(response, result);
  }

  @Test
  void testEmptyBatch() throws Exception {
    SpanBatchSender testClass = new SpanBatchSender(null, null);
    SpanBatch batch = new SpanBatch(Collections.emptyList(), new Attributes());
    Response response = testClass.sendBatch(batch);
    assertEquals(202, response.getStatusCode());
  }
}
