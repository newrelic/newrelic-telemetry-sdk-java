package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AttributesTest {

  @Test
  void testCopyEmpty() {
    Attributes testClass = new Attributes();
    Attributes result = testClass.copy();
    assertNotSame(result, testClass);
    assertTrue(result.isEmpty());
  }

  @Test
  public void testCopyDoesntMutateOrigin() throws Exception {
    Attributes testClass = new Attributes().put("foo", "bar");
    Attributes result = testClass.copy().put("bar", "baz");
    assertNotEquals(result, testClass);
    assertNotSame(result, testClass);
    assertNull(testClass.asMap().get("bar"));
    assertEquals("baz", result.asMap().get("bar"));
  }

  @Test
  public void testCopyIsEqual() throws Exception {
    Attributes testClass = new Attributes().put("a", "b");
    Attributes result = testClass.copy();
    assertEquals(result, testClass);
  }

}