package com.newrelic.telemetry;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MetricBatchJsonTest {
    private MetricJsonGenerator metricJsonGenerator;
    private AttributesJson attributeJson;
    private Gauge gauge;
    private Attributes commonAttributes;
    private MetricBatch metricBatch;

    @BeforeEach
    void setup() {
        commonAttributes = new Attributes().put("key", "val");
        gauge = new Gauge("gauge", 3d, 555, new Attributes());
        metricBatch = new MetricBatch(Collections.singletonList(gauge), commonAttributes);
        metricJsonGenerator = mock(MetricJsonGenerator.class);
        attributeJson = mock(AttributesJson.class);
    }

  @Test
  @DisplayName("Formatting with common attributes is structured correctly")
  void testCommonJsonBlock() throws Exception {
      String expectedCommonJsonBlock = "\"common\":{\"attributes\":{\"key\":\"val\"}}";
      when(attributeJson.toJson(commonAttributes.asMap())).thenReturn("{\"key\":\"val\"}");

      StringBuilder stringBuilder = new StringBuilder();
      MetricBatchJson metricBatchJson = new MetricBatchJson(metricJsonGenerator, attributeJson);
      metricBatchJson.appendCommonJson(metricBatch, stringBuilder);

      JSONAssert.assertEquals(expectedCommonJsonBlock, stringBuilder.toString(), false);
  }

  @Test
  @DisplayName("Formatting with telemetry attributes is structured correctly")
  void testTelemetryJsonBlock() throws Exception {
      String expectedTelemetryJsonBlock = "\"metrics\":[{\"name\":\"gauge\",\"type\":\"gauge\",\"value\":3.0,\"timestamp\":555}]";
      when(metricJsonGenerator.writeGaugeJson(gauge)).thenReturn("{\"name\":\"gauge\",\"type\":\"gauge\",\"value\":3.0,\"timestamp\":555}");

      StringBuilder stringBuilder = new StringBuilder();
      MetricBatchJson metricBatchJson = new MetricBatchJson(metricJsonGenerator, attributeJson);
      metricBatchJson.appendTelemetry(metricBatch, stringBuilder);

      JSONAssert.assertEquals(expectedTelemetryJsonBlock, stringBuilder.toString(), false);
  }
}
