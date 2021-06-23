package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
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

  @Test
  void defaultEndpointTest() throws Exception {
    URL testEndpointURL = new URL(testURL + testPath);
    SenderConfiguration testConfig = SenderConfiguration.builder(testURL, testPath).build();
    assertEquals(testEndpointURL, testConfig.getEndpointUrl());
  }

  @Test
  void userProvidedEndpointTest() throws Exception {
    URL testEndpointURL = new URL("https://google.com");
    SenderConfiguration testConfig =
        SenderConfiguration.builder(testURL, testPath).endpoint(testEndpointURL).build();
    assertEquals(testEndpointURL, testConfig.getEndpointUrl());
  }

  @Test
  void defaultEndpointAsStringTest() throws Exception {
    String endpointURLAsString = testURL + testPath;
    SenderConfiguration testConfig = SenderConfiguration.builder(testURL, testPath).build();
    assertEquals(endpointURLAsString, testConfig.getEndpointUrl().toString());
  }

  @Test
  void userEndpointAsStringTest() throws Exception {
    URL testEndpointURL = new URL("https://google.com");
    String endpointURLAsString = "https://google.com";
    SenderConfiguration testConfig =
        SenderConfiguration.builder(testURL, testPath).endpoint(testEndpointURL).build();
    assertEquals(endpointURLAsString, testConfig.getEndpointUrl().toString());
  }
}
