package com.newrelic.telemetry;

import com.newrelic.telemetry.metrics.Gauge;
import com.newrelic.telemetry.metrics.Metric;
import com.newrelic.telemetry.metrics.MetricBatch;
import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;

public class NotificationHandlerTest {

  @Test
  void testNotificationHandler() {
    Logger log = Mockito.mock(Logger.class);
    NotificationHandler notificationHandler = new LoggingNotificationHandler(log);
    RuntimeException e = new RuntimeException("oops");
    notificationHandler.noticeError("An Error", e, null);
    Mockito.verify(log).error("An Error", e);

    notificationHandler.noticeInfo("A message", null);
    Mockito.verify(log).info("A message", (Throwable) null);
  }

  @Test
  void shouldAddMetricBatchTypeToMessage() {
    Logger log = Mockito.mock(Logger.class);
    Attributes attributes = new Attributes();
    Collection<Metric> metrics = Arrays.asList(new Gauge("Nicholas Gauge", 4.0, 45L, attributes));
    MetricBatch batch = new MetricBatch(metrics, attributes);
    NotificationHandler notificationHandler = new LoggingNotificationHandler(log);
    RuntimeException e = new RuntimeException("oops");

    notificationHandler.noticeError("An Error", e, batch);
    Mockito.verify(log).error("[MetricBatch] - An Error", e);

    notificationHandler.noticeInfo("A message", batch);
    Mockito.verify(log).info("[MetricBatch] - A message", (Throwable) null);
  }
}
