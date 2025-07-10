/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class AttributesTest {

  @Test
  void testCopyMethod() {
    Attributes a = new Attributes().put("superFoo", "openBar").put("extraFuzz", "extraJazz");
    Attributes copyOfA = a.copy();
    assertEquals(2, copyOfA.asMap().size());
    assertTrue(copyOfA.asMap().containsKey("superFoo"));
    assertTrue(copyOfA.asMap().containsKey("extraFuzz"));

    a.put("hello", "goodbye");
    assertTrue(a.asMap().containsKey("superFoo"));
    assertTrue(a.asMap().containsKey("hello"));
    assertFalse(copyOfA.asMap().containsKey("hello"));

    copyOfA.put("keyOnlyInCopy", "valueOnlyInCopy");
    assertTrue(copyOfA.asMap().containsKey("keyOnlyInCopy"));
    assertFalse(a.asMap().containsKey("keyOnlyInCopy"));
  }

  @Test
  void testCopyConstructor() {
    Attributes a = new Attributes().put("superFoo", "openBar").put("extraFuzz", "extraJazz");
    Attributes copyOfA = new Attributes(a);
    assertEquals(2, copyOfA.asMap().size());
    assertTrue(copyOfA.asMap().containsKey("superFoo"));
    assertTrue(copyOfA.asMap().containsKey("extraFuzz"));

    a.put("hello", "goodbye");
    assertTrue(a.asMap().containsKey("superFoo"));
    assertTrue(a.asMap().containsKey("hello"));
    assertFalse(copyOfA.asMap().containsKey("hello"));

    copyOfA.put("keyOnlyInCopy", "valueOnlyInCopy");
    assertTrue(copyOfA.asMap().containsKey("keyOnlyInCopy"));
    assertFalse(a.asMap().containsKey("keyOnlyInCopy"));
  }

  @Test
  void testContainsKey() {
    Attributes a = new Attributes().put("foo", "bar");
    assertTrue(a.containsKey("foo"));
    assertFalse(a.containsKey("zzz"));
  }

  @Test
  void testRemove() {
    Attributes a = new Attributes().put("foo", "bar");
    a.put("biz", "baz");
    assertTrue(a.asMap().containsKey("foo"));

    a.remove("foo");
    assertFalse(a.asMap().containsKey("foo"));
  }
}
