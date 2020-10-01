package com.newrelic.telemetry.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.BaseConfig;
import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.events.json.EventBatchMarshaller;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.http.HttpResponse;
import com.newrelic.telemetry.transport.BatchDataSender;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class EventBatchSenderTest {

  @Test
  void testSplitSend() throws Exception {
    Event el = new Event("JitThing1", null, System.currentTimeMillis());
    Event el2 = new Event("JitThing2", null, System.currentTimeMillis());
    List<Event> events = new ArrayList<>();
    events.add(el);
    events.add(el2);
    Attributes common = new Attributes().put("j", "k");
    EventBatch batch = new EventBatch(events, common);

    String json = "{\"a\":\"great document\"}";
    EventBatchMarshaller marshaller = mock(EventBatchMarshaller.class);
    when(marshaller.toJson(any())).thenReturn(json);

    Response ok = new Response(200, "OK", "yup");
    BatchDataSender sender = mock(BatchDataSender.class);
    when(sender.send(json, batch.getUuid()))
        .thenThrow(RetryWithSplitException.class)
        .thenReturn(ok);

    EventBatchSender testClass = new EventBatchSender(marshaller, sender);

    assertThrows(
        RetryWithSplitException.class,
        () -> {
          testClass.sendBatch(batch);
        });
  }

  @Test
  void testEmptyBatch() throws Exception {
    EventBatchSender testClass = new EventBatchSender(null, null);
    EventBatch batch = new EventBatch(Collections.emptyList(), new Attributes());
    Response response = testClass.sendBatch(batch);
    assertEquals(202, response.getStatusCode());
  }

  @Test
  public void sendBatchViaCreate() throws Exception {
    BaseConfig baseConfig = new BaseConfig("hi", true, "second");
    Response expected = new Response(202, "okey", "bb");
    HttpResponse httpResponse =
        new HttpResponse(
            expected.getBody(),
            expected.getStatusCode(),
            expected.getStatusMessage(),
            new HashMap<>());
    URL url = URI.create("https://insights-collector.newrelic.com/v1/accounts/events").toURL();

    HttpPoster poster = mock(HttpPoster.class);
    Event event = new Event("mytype", new Attributes().put("a", "b"));
    Collection<Event> events = Collections.singletonList(event);
    EventBatch batch = new EventBatch(events, new Attributes().put("f", "b"));
    Supplier<HttpPoster> posterSupplier = () -> poster;

    ArgumentCaptor<Map> headersCaptor = ArgumentCaptor.forClass(Map.class);
    when(poster.post(eq(url), headersCaptor.capture(), isA(byte[].class), anyString()))
        .thenReturn(httpResponse);

    EventBatchSender logBatchSender = EventBatchSender.create(posterSupplier, baseConfig);

    Response result = logBatchSender.sendBatch(batch);
    assertEquals(expected, result);
    assertTrue(((String) headersCaptor.getValue().get("User-Agent")).endsWith(" second"));
  }
}
