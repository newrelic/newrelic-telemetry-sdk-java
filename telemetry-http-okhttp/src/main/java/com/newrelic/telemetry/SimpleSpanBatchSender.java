package com.newrelic.telemetry;

import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.spans.SpanBatchSender;
import com.newrelic.telemetry.spans.SpanBatchSenderBuilder;
import java.time.Duration;

/**
 * A builder class for creating a SpanBatchSender that uses okhttp as the underlying http client
 * implementation.
 *
 * <p>Note: This class will be deprecated in the near future and be replaced with something named
 * more succinctly.
 */
public class SimpleSpanBatchSender {

  /**
   * Create a new SpanBatchSender with your New Relic Insights Insert API key, and otherwise default
   * settings. (2 second timeout, audit logging off, with the default endpoint URL)
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  public static SpanBatchSender build(String apiKey) {
    return build(apiKey, Duration.ofSeconds(2));
  }

  /**
   * Create a new SpanBatchSenderBuilder with your New Relic Insights Insert API key.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  public static SpanBatchSenderBuilder builder(String apiKey) {
    return builder(apiKey);
  }

  /**
   * Create a new SpanBatchSender with your New Relic Insights Insert API key and a custom http
   * timeout; Otherwise default settings. (audit logging off and with the default endpoint URL)
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  public static SpanBatchSender build(String apiKey, Duration callTimeout) {
    return builder(apiKey, callTimeout).build();
  }

  /**
   * Create a new SpanBatchSenderBuilder with your New Relic Insights Insert API key and a custom
   * http timeout.
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  public static SpanBatchSenderBuilder builder(String apiKey, Duration callTimeout) {
    HttpPoster http = new OkHttpPoster(callTimeout);
    return SpanBatchSender.builder().apiKey(apiKey).httpPoster(http);
  }
}
