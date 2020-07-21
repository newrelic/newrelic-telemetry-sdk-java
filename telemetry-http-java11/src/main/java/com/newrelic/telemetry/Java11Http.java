package com.newrelic.telemetry;

/** Simple way to create a TelemetryClient that is backed by a Java11+ based HTTP poster. */
public class Java11Http {

  /**
   * Create a new TelemetryClient with the given apiKey and default configuration
   *
   * @param apiKey - The New Relic insert api key
   */
  public static TelemetryClient newTelemetryClient(String apiKey) {
    return TelemetryClient.create(Java11HttpPoster::new, apiKey);
  }

  /**
   * Create a new TelemetryClient based on the configuration in the BaseConfig instance passed in.
   * Everything not covered in the baseConfig will be defaulted.
   *
   * @param baseConfig - A configuration instance with the basics
   */
  public static TelemetryClient newTelemetryClient(BaseConfig baseConfig) {
    return TelemetryClient.create(Java11HttpPoster::new, baseConfig);
  }
}
