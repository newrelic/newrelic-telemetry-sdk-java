package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SenderConfigurationTest {

  String testURL = "https://fun.com";
  String testPath = "/account/metric";

  @Test
  void defaultRegionTest() {
    SenderConfiguration testConfig = SenderConfiguration.builder(testURL, testPath).build();
    assertEquals("US", testConfig.getRegion());
  }

  @Test
  void defaultEURegionTest() {
    SenderConfiguration testEUConfig =
        SenderConfiguration.builder(testURL, testPath).setRegion("EU").build();
    assertEquals("EU", testEUConfig.getRegion());
  }

  @Test
  void exceptionTest() {
    Exception testIllegalArgumentException =
        assertThrows(
            IllegalArgumentException.class,
            () -> {
              SenderConfiguration.builder(testURL, testPath).setRegion("ASIA").build();
            });
    String expectedExceptionMessage = "The only supported regions are the US and EU regions";
    assertEquals(expectedExceptionMessage, testIllegalArgumentException.getMessage());
  }
}
