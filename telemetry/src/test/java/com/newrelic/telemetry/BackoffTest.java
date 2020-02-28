package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class BackoffTest {

  @Test
  void testBackoff() throws Exception {
    Backoff testClass = new Backoff(80000, 5000, 8);
    assertEquals(0, testClass.nextWaitMs());
    assertEquals(5000, testClass.nextWaitMs());
    assertEquals(10000, testClass.nextWaitMs());
    assertEquals(20000, testClass.nextWaitMs());
    assertEquals(40000, testClass.nextWaitMs());
    assertEquals(80000, testClass.nextWaitMs());
    assertEquals(80000, testClass.nextWaitMs());
    assertEquals(80000, testClass.nextWaitMs());
    assertEquals(-1, testClass.nextWaitMs());
    assertEquals(-1, testClass.nextWaitMs());
  }

  @Test
  void testDefault() throws Exception {
    Backoff testClass = Backoff.defaultBackoff();
    assertEquals(0, testClass.nextWaitMs());
    assertEquals(1000, testClass.nextWaitMs());
    assertEquals(2000, testClass.nextWaitMs());
    assertEquals(4000, testClass.nextWaitMs());
    assertEquals(8000, testClass.nextWaitMs());
    assertEquals(15000, testClass.nextWaitMs());
    assertEquals(15000, testClass.nextWaitMs());
    assertEquals(15000, testClass.nextWaitMs());
    assertEquals(15000, testClass.nextWaitMs());
    assertEquals(15000, testClass.nextWaitMs());
    assertEquals(-1, testClass.nextWaitMs());
    assertEquals(-1, testClass.nextWaitMs());
  }
}
