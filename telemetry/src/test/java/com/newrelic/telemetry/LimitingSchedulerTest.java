package com.newrelic.telemetry;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LimitingSchedulerTest {

  private ScheduledExecutorService exec;

  @BeforeEach
  void setup() {
    exec = Executors.newSingleThreadScheduledExecutor();
  }

  @Test
  void testScheduleNoProblem() throws Exception {
    LimitingScheduler testClass = new LimitingScheduler(exec, 10);
    CountDownLatch completed = new CountDownLatch(2);
    boolean result1 = testClass.schedule(5, completed::countDown);
    boolean result2 = testClass.schedule(5, completed::countDown);
    assertTrue(completed.await(5, SECONDS));
    assertTrue(result1);
    assertTrue(result2);
  }

  @Test
  void testScheduleNoFailsOverCapacity() throws Exception {
    ConcurrentHashMap<String, Object> seen = new ConcurrentHashMap<>();
    CountDownLatch latch = new CountDownLatch(1);
    LimitingScheduler testClass = new LimitingScheduler(exec, 10);
    boolean result1 =
        testClass.schedule(
            6,
            () -> {
              seen.put("1", "");
              latch.countDown();
            },
            13,
            TimeUnit.MILLISECONDS);
    boolean result2 =
        testClass.schedule(
            6,
            () -> {
              seen.put("2", "");
              latch.countDown();
            },
            13,
            TimeUnit.MILLISECONDS);
    assertTrue(latch.await(5, SECONDS));
    assertTrue(result1);
    assertFalse(result2);
    assertEquals(new HashSet<>(Collections.singletonList("1")), seen.keySet());
    boolean result3 = testClass.schedule(6, () -> {}, 13, TimeUnit.MILLISECONDS);
    assertTrue(result3);
  }

  @Test
  void testFirstSuccessSecondFailsRetry() throws Exception {
    LimitingScheduler testClass = new LimitingScheduler(exec, 10);
    AtomicBoolean wasRun = new AtomicBoolean(false);
    boolean result =
        testClass.schedule(
            500,
            () -> {
              wasRun.set(true);
            });
    assertFalse(result);
    assertFalse(wasRun.get());
  }

  @Test
  public void testDelegates() throws Exception {
    ScheduledExecutorService delegate = mock(ScheduledExecutorService.class);
    LimitingScheduler testClass = new LimitingScheduler(delegate, 12);
    testClass.awaitTermination(4, SECONDS);
    verify(delegate).awaitTermination(4, SECONDS);
    testClass.isTerminated();
    verify(delegate).isTerminated();
    testClass.shutdown();
    verify(delegate).shutdown();
    testClass.shutdownNow();
    verify(delegate).shutdownNow();
  }
}
