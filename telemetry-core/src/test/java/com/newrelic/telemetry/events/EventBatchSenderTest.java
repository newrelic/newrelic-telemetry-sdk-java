package com.newrelic.telemetry.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.BaseConfig;
import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.events.json.EventBatchMarshaller;
import com.newrelic.telemetry.exceptions.RetryWithSplitException;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.http.HttpResponse;
import com.newrelic.telemetry.transport.BatchDataSender;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.*;
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
    when(sender.send(json, batch)).thenThrow(RetryWithSplitException.class).thenReturn(ok);

    EventBatchSender testClass = new EventBatchSender(marshaller, sender);

    ArrayList<EventBatch> testEventBatch = new ArrayList<EventBatch>();
    testEventBatch.add(batch);

    assertThrows(
        RetryWithSplitException.class,
        () -> {
          testClass.sendBatch(testEventBatch);
        });
  }

  @Test
  void testEmptyBatch() throws Exception {
    EventBatchSender testClass = new EventBatchSender(null, null);
    EventBatch batch = new EventBatch(Collections.emptyList(), new Attributes());

    ArrayList<EventBatch> testBatchList = new ArrayList<EventBatch>();
    testBatchList.add(batch);

    Response response = testClass.sendBatch(testBatchList);
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

    ArrayList<EventBatch> testBatchList = new ArrayList<EventBatch>();
    testBatchList.add(batch);

    Response result = logBatchSender.sendBatch(testBatchList);

    assertEquals(expected, result);
    assertTrue(((String) headersCaptor.getValue().get("User-Agent")).endsWith(" second"));
  }

  @Test
  public void testDefaultEndpoint() throws Exception {
    URL testURL = new URL("https://insights-collector.newrelic.com/v1/accounts/events");

    EventBatchMarshaller testMarshaller = mock(EventBatchMarshaller.class);
    BatchDataSender testSender = mock(BatchDataSender.class);

    EventBatchSender testEventBatchSender = new EventBatchSender(testMarshaller, testSender);
    assertEquals(testURL, testEventBatchSender.returnEndpoint("US"));
  }

  @Test
  public void testEUEndpoint() throws Exception {
    URL testEUURL = new URL("https://insights-collector.eu01.nr-data.net/v1/accounts/events");

    EventBatchMarshaller testMarshaller = mock(EventBatchMarshaller.class);
    BatchDataSender testSender = mock(BatchDataSender.class);

    EventBatchSender testEventBatchSender = new EventBatchSender(testMarshaller, testSender);
    assertEquals(testEUURL, testEventBatchSender.returnEndpoint("EU"));
  }

  @Test
  public void testException() {
    EventBatchMarshaller testMarshaller = mock(EventBatchMarshaller.class);
    BatchDataSender testSender = mock(BatchDataSender.class);

    EventBatchSender testEventBatchSender = new EventBatchSender(testMarshaller, testSender);
    Exception testMalformedURLException =
        assertThrows(
            MalformedURLException.class,
            () -> {
              testEventBatchSender.returnEndpoint("random");
            });
    String expectedExceptionMessage =
        "A valid region (EU or US) needs to be added to generate the right endpoint";
    assertEquals(expectedExceptionMessage, testMalformedURLException.getMessage());
  }
}
