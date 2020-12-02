package com.newrelic.telemetry;

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
}
