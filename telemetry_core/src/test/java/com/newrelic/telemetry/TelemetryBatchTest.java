package com.newrelic.telemetry;

import static java.util.Collections.emptyList;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.newrelic.telemetry.Telemetry.Type;
import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.Metric;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class TelemetryBatchTest {

  @Test
  void testEmpty() {
    TelemetryBatch<Metric> batch1 =
        new TelemetryBatch<Metric>(Type.METRIC, emptyList(), new Attributes());
    Metric metric = new Count("a", 12.0, 123, 456, new Attributes());
    TelemetryBatch<Metric> batch2 =
        new TelemetryBatch<Metric>(Type.METRIC, Collections.singleton(metric), new Attributes());
    assertTrue(batch1.isEmpty());
    assertFalse(batch2.isEmpty());
  }
}
