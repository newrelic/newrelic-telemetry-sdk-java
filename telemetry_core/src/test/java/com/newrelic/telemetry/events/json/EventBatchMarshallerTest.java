package com.newrelic.telemetry.events.json;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.events.Event;
import com.newrelic.telemetry.events.EventBatch;
import com.newrelic.telemetry.json.AttributesJson;
import java.math.BigDecimal;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class EventBatchMarshallerTest {

  private EventBatchMarshaller eventBatchMarshaller;

  @BeforeEach
  void setup() {
    AttributesJson attributesJson = new AttributesJson();
    eventBatchMarshaller =
        new EventBatchMarshaller(
            new EventBatchJsonCommonBlockWriter(attributesJson),
            new EventBatchJsonTelemetryBlockWriter());
  }

  @Test
  public void test_simple_serialize() throws Exception {
    Attributes commonAttributes = new Attributes();
    commonAttributes.put("double", 3.14d);
    commonAttributes.put("float", 4.32f);
    commonAttributes.put("int", 5);
    commonAttributes.put("long", 384949494949499999L);
    commonAttributes.put("boolean", true);
    commonAttributes.put("number", new BigDecimal("55.555"));
    commonAttributes.put("null", (Number) null);

    EventBatch eb =
        new EventBatch(
            Collections.singletonList(new Event("testJIT", new Attributes(), 1586413929145L)),
            commonAttributes);

    String json = eventBatchMarshaller.toJson(eb);

    String expected =
        "[{\"metrics\":[{\"timestamp\":1586413929145,\"eventType\":\"testJIT\",\"attributes\":{\"number\":55.555,\"boolean\":true,\"double\":3.14,\"float\":4.32,\"int\":5,\"long\":384949494949499999}}]}]";
    JSONAssert.assertEquals(expected, json, false);
  }
}
