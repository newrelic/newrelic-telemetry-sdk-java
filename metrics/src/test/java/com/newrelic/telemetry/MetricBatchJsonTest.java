package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class MetricBatchJsonTest {

  @Test
  @DisplayName("Formatting with common attributes is structured correctly")
  void testCommonJson() throws Exception {

    StringBuilder builder = new StringBuilder();
    Attributes commonAttributes = new Attributes().put("key", "val");

    Gauge gauge = new Gauge("gauge", 3d, 555, new Attributes());
    MetricBatch batch = new MetricBatch(Collections.singletonList(gauge), commonAttributes);

    MetricJsonGenerator jsonGen = mock(MetricJsonGenerator.class);
    AttributesJson attributeJson = mock(AttributesJson.class);

    when(jsonGen.writeGaugeJson(gauge))
        .thenAnswer(
            inv -> {
              StringBuilder sb = inv.getArgument(1);
              sb.append("{\"gauge\":{\"foo\":\"bar\"}");
              return null;
            });

    TelemetryBatchJson metricBatchJson = MetricBatchJson.build(jsonGen, attributeJson);
    String result = metricBatchJson.toJson(batch);

    String expected =
        "[{\"common\":{\"attributes\":{\"key\":\"val\"}},\"metrics\":"
            + "[{\"name\":\"gauge\",\"type\":\"gauge\",\"value\":3.0,\"timestamp\":555}]}]";
    JSONAssert.assertEquals(expected, result, false);
  }

  @Test
  void testTelemetryBlock() {
    fail("build me");
  }
}
