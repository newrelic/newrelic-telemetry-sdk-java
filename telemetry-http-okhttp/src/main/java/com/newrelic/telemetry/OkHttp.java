package com.newrelic.telemetry;

/** Simple way to create a TelemetryClient that is backed by an OkHttp based HTTP posters. */
public class OkHttp {

  /**
   * Create a new TelemetryClient with the given apiKey and default configuration
   *
   * @param apiKey - The New Relic insert api key
   * @return a new instance of a Telemetry Client
   */
  public static TelemetryClient newTelemetryClient(String apiKey) {
    return TelemetryClient.create(OkHttpPoster::new, apiKey);
  }

  /**
   * Create a new TelemetryClient based on the configuration in the BaseConfig instance passed in.
   * Everything not covered in the baseConfig will be defaulted.
   *
   * @param baseConfig - A configuration instance with the basics
   * @return a new instance of a Telemetry Client
   */
  public static TelemetryClient newTelemetryClient(BaseConfig baseConfig) {
    return TelemetryClient.create(OkHttpPoster::new, baseConfig);
  }
}
