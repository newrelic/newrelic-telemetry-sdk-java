package com.newrelic.telemetry.spans;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import com.newrelic.telemetry.http.HttpPoster;
import org.junit.jupiter.api.Test;

class SpanBatchSenderBuilderTest {

  @Test
  void testBuild() {
    SpanBatchSender result =
        new SpanBatchSenderBuilder().apiKey("123").httpPoster(mock(HttpPoster.class)).build();
    assertNotNull(result);
  }

  @Test
  void testMissingApiKey() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new SpanBatchSenderBuilder().httpPoster(mock(HttpPoster.class)).build());
  }

  @Test
  void testMissingHttpPoster() {
    assertThrows(
        IllegalArgumentException.class, () -> new SpanBatchSenderBuilder().apiKey("123").build());
  }
}
