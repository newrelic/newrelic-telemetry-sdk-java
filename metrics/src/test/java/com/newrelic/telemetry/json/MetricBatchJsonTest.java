package com.newrelic.telemetry.json;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Gauge;
import com.newrelic.telemetry.MetricBatch;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class MetricBatchJsonTest {
  private MetricToJson metricToJson;
  private AttributesJson attributeJson;
  private Gauge gauge;
  private Attributes commonAttributes;
  private MetricBatch metricBatch;

  @BeforeEach
  void setup() {
    commonAttributes = new Attributes().put("key", "val");
    gauge = new Gauge("gauge", 3d, 555, new Attributes());
    metricBatch = new MetricBatch(Collections.singletonList(gauge), commonAttributes);
    metricToJson = mock(MetricToJson.class);
    attributeJson = mock(AttributesJson.class);
  }

  @Test
  @DisplayName("Formatting with common attributes is structured correctly")
  void testCommonJsonBlock() throws Exception {
    String expectedCommonJsonBlock = "\"common\":{\"attributes\":{\"key\":\"val\"}}";
    when(attributeJson.toJson(commonAttributes.asMap())).thenReturn("{\"key\":\"val\"}");

    StringBuilder stringBuilder = new StringBuilder();
    MetricBatchJson metricBatchJson = new MetricBatchJson(metricToJson, attributeJson);
    metricBatchJson.appendCommonJson(metricBatch, stringBuilder);

    JSONAssert.assertEquals(expectedCommonJsonBlock, stringBuilder.toString(), false);
  }

  @Test
  @DisplayName("Formatting with telemetry attributes is structured correctly")
  void testTelemetryJsonBlock() throws Exception {
    String expectedTelemetryJsonBlock =
        "\"metrics\":[{\"name\":\"gauge\",\"type\":\"gauge\",\"value\":3.0,\"timestamp\":555}]";
    when(metricToJson.writeGaugeJson(gauge))
        .thenReturn("{\"name\":\"gauge\",\"type\":\"gauge\",\"value\":3.0,\"timestamp\":555}");

    StringBuilder stringBuilder = new StringBuilder();
    MetricBatchJson metricBatchJson = new MetricBatchJson(metricToJson, attributeJson);
    metricBatchJson.appendTelemetry(metricBatch, stringBuilder);

    JSONAssert.assertEquals(expectedTelemetryJsonBlock, stringBuilder.toString(), false);
  }
}
