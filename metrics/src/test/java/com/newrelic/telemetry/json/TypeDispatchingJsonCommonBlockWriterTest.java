package com.newrelic.telemetry.json;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Gauge;
import com.newrelic.telemetry.Metric;
import com.newrelic.telemetry.Telemetry.Type;
import com.newrelic.telemetry.TelemetryBatch;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class TypeDispatchingJsonCommonBlockWriterTest {

  @Test
  void testMetrics() {
    StringBuilder sb = new StringBuilder();
    Collection<Metric> telemetry = Collections
        .singleton(new Gauge("one", 1, 1, new Attributes().put("foo", "bar")));
    TelemetryBatch<Metric> batch = new TelemetryBatch<>(Type.METRIC, telemetry,
        new Attributes().put("bar", "baz"));

    JsonCommonBlockWriter metricsWriter = mock(JsonCommonBlockWriter.class);

    TypeDispatchingJsonCommonBlockWriter testClass = new TypeDispatchingJsonCommonBlockWriter(
        metricsWriter, null);

    testClass.appendCommonJson(batch, sb);
    verify(metricsWriter).appendCommonJson(batch, sb);
  }

  @Test
  void testSpans() {
    fail("build me");
  }

}