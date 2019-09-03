package com.newrelic.telemetry.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Gauge;
import com.newrelic.telemetry.Metric;
import com.newrelic.telemetry.MetricBatch;
import com.newrelic.telemetry.TelemetryBatch;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MetricBatchJsonCommonBlockWriterTest {

  private AttributesJson attributesJson;
  private Attributes commonAttributes;
  private TelemetryBatch<Metric> metricBatch;
  private Gauge gauge;

  @BeforeEach
  void setup() {
    commonAttributes = new Attributes().put("key", "val");
    attributesJson = mock(AttributesJson.class);
    gauge = new Gauge("gauge", 3d, 555, new Attributes());
    metricBatch = new MetricBatch(Collections.singletonList(gauge), commonAttributes);
  }

  @Test
  void testNoCommonAttributes() throws Exception {
    StringBuilder sb = new StringBuilder();
    MetricBatchJsonCommonBlockWriter testClass =
        new MetricBatchJsonCommonBlockWriter(attributesJson);
    metricBatch = new MetricBatch(Collections.singletonList(gauge), new Attributes());
    testClass.appendCommonJson(metricBatch, sb);
    assertEquals("", sb.toString());
  }

  @Test
  void testCommonJson() throws Exception {
    String expectedCommonJsonBlock = "\"common\":{\"attributes\":{\"key\":\"val\"}}";
    when(attributesJson.toJson(commonAttributes.asMap())).thenReturn("{\"key\":\"val\"}");

    StringBuilder sb = new StringBuilder();
    MetricBatchJsonCommonBlockWriter testClass =
        new MetricBatchJsonCommonBlockWriter(attributesJson);
    testClass.appendCommonJson(metricBatch, sb);

    assertEquals(expectedCommonJsonBlock, sb.toString());
  }
}
