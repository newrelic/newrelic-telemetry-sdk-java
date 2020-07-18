package com.newrelic.telemetry;

import com.newrelic.telemetry.http.HttpPoster;
import java.util.function.Supplier;

/** Simple way to create a TelemetryClient that is backed by an OkHttp based HTTP posters. */
public class OkHttp {

  private static final Supplier<HttpPoster> supplier = OkHttpPoster::new;

  /**
   * Create a new TelemetryClient with the given apiKey and default configuration
   *
   * @param apiKey - The New Relic insert api key
   */
  public static TelemetryClient newTelemetryClient(String apiKey) {
    return TelemetryClient.create(supplier, apiKey);
  }

  /**
   * Create a new TelemetryClient based on the configuration in the BaseConfig instance passed in.
   * Everything not covered in the baseConfig will be defaulted.
   *
   * @param baseConfig - A configuration instance with the basics
   */
  public static TelemetryClient newTelemetryClient(BaseConfig baseConfig) {
    return TelemetryClient.create(supplier, baseConfig);
  }
}
