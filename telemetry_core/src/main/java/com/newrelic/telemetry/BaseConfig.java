package com.newrelic.telemetry;

/** A configuration class for several common settings. */
public class BaseConfig {

  private final String apiKey;
  private final boolean auditLoggingEnabled;
  private final String secondaryUserAgent;

  /**
   * Create a new BaseConfig with a required apiKey. Audit logging will default to disabled and
   * there will be no secondary user agent.
   *
   * @param apiKey the API insert key required for the sdk to send telemetry.
   */
  public BaseConfig(String apiKey) {
    this(apiKey, false);
  }

  /**
   * Create a new BaseConfig with a required apiKey and a setting for auditLoggingEnabled. If
   * auditLoggingEnabled is true, the SDK will be extra verbose, which can help when
   * troubleshooting. There will be no secondary user agent.
   *
   * @param apiKey The API insert key required for the sdk to send telemetry.
   * @param auditLoggingEnabled true to turn on audit/verbose logging
   */
  public BaseConfig(String apiKey, boolean auditLoggingEnabled) {
    this(apiKey, auditLoggingEnabled, null);
  }

  /**
   * Creates a new BaseConfig.
   *
   * @param apiKey the API insert key required for the sdk to send telemetry.
   * @param auditLoggingEnabled true to turn on audit/verbose logging
   * @param secondaryUserAgent an extra string to put into the HTTP user agent
   */
  public BaseConfig(String apiKey, boolean auditLoggingEnabled, String secondaryUserAgent) {
    this.apiKey = apiKey;
    this.auditLoggingEnabled = auditLoggingEnabled;
    this.secondaryUserAgent = secondaryUserAgent;
  }

  /** @return the New Relic api key */
  public String getApiKey() {
    return apiKey;
  }

  /** @return true if verbose audit logging is enabled */
  public boolean isAuditLoggingEnabled() {
    return auditLoggingEnabled;
  }

  /** @return the secondary http user agent string, which may be null */
  public String getSecondaryUserAgent() {
    return secondaryUserAgent;
  }
}
