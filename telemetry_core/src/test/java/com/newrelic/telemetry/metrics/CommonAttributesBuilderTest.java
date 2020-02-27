package com.newrelic.telemetry.metrics;

import com.newrelic.telemetry.Attributes;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommonAttributesBuilderTest {

  @Test
  void testEmpty() throws Exception {
    assertEquals(new Attributes(), new CommonAttributesBuilder().build());
  }

  @Test
  public void testBasic() throws Exception {
    Attributes attributes = new Attributes().put("foo", "bar").put("a", "b");
    CommonAttributesBuilder builder = new CommonAttributesBuilder().attributes(attributes);
    assertEquals(attributes, builder.build());
  }

  @Test
  public void testAll() throws Exception {
    Attributes attributes = new Attributes().put("a", "b");
    String serviceName = "silver";
    String instrumentationProvider = "gold";
    Attributes expectedAttributes =
        new Attributes(attributes)
            .put("service.name", serviceName)
            .put("instrumentation.provider", instrumentationProvider);
    CommonAttributesBuilder builder =
        new CommonAttributesBuilder()
            .attributes(attributes)
            .serviceName(serviceName)
            .instrumentationProvider(instrumentationProvider);
    assertEquals(expectedAttributes, builder.build());
  }
}
