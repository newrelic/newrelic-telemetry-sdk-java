package com.newrelic.telemetry;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AttributesTest {

  @Test
  void testCopyMethod() {
    Attributes a = new Attributes().put("superFoo", "openBar");
    Attributes copyOfA = a.copy();

    a.put("hello", "goodbye");
    Assertions.assertTrue(a.asMap().containsKey("superFoo"));
    Assertions.assertTrue(a.asMap().containsKey("hello"));
    Assertions.assertFalse(copyOfA.asMap().containsKey("hello"));

    copyOfA.put("keyOnlyInCopy", "valueOnlyInCopy");
    Assertions.assertTrue(copyOfA.asMap().containsKey("keyOnlyInCopy"));
    Assertions.assertFalse(a.asMap().containsKey("keyOnlyInCopy"));
  }

  @Test
  void testCopyConstructor() {
    Attributes a = new Attributes().put("superFoo", "openBar");
    Attributes copyOfA = new Attributes(a);

    a.put("hello", "goodbye");
    Assertions.assertTrue(a.asMap().containsKey("superFoo"));
    Assertions.assertTrue(a.asMap().containsKey("hello"));
    Assertions.assertFalse(copyOfA.asMap().containsKey("hello"));

    copyOfA.put("keyOnlyInCopy", "valueOnlyInCopy");
    Assertions.assertTrue(copyOfA.asMap().containsKey("keyOnlyInCopy"));
    Assertions.assertFalse(a.asMap().containsKey("keyOnlyInCopy"));
  }
}
