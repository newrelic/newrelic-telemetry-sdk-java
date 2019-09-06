package com.newrelic.telemetry.spans;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.json.AttributesJson;
import org.junit.jupiter.api.Test;

class SpanBatchSenderBuilderTest {

  @Test
  void testBuild() {
    SpanBatchSender result =
        new SpanBatchSenderBuilder()
            .apiKey("123")
            .attributesJson(mock(AttributesJson.class))
            .httpPoster(mock(HttpPoster.class))
            .build();
    assertNotNull(result);
  }

  @Test
  void testMissingApiKey() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new SpanBatchSenderBuilder()
                .attributesJson(mock(AttributesJson.class))
                .httpPoster(mock(HttpPoster.class))
                .build());
  }

  @Test
  void testMissingAttributesJson() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new SpanBatchSenderBuilder().apiKey("123").httpPoster(mock(HttpPoster.class)).build());
  }

  @Test
  void testMissingHttpPoster() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new SpanBatchSenderBuilder()
                .apiKey("123")
                .attributesJson(mock(AttributesJson.class))
                .build());
  }
}
