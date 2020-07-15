package com.newrelic.telemetry;

public class BaseConfig {

  private final String apiKey;
  private final boolean auditLoggingEnabled;
  private final String secondaryUserAgent;

  public BaseConfig(String apiKey) {
    this(apiKey, false);
  }

  public BaseConfig(String apiKey, boolean auditLoggingEnabled) {
    this(apiKey, auditLoggingEnabled, null);
  }

  public BaseConfig(String apiKey, boolean auditLoggingEnabled, String secondaryUserAgent) {
    this.apiKey = apiKey;
    this.auditLoggingEnabled = auditLoggingEnabled;
    this.secondaryUserAgent = secondaryUserAgent;
  }

  public String getApiKey() {
    return apiKey;
  }

  public boolean isAuditLoggingEnabled() {
    return auditLoggingEnabled;
  }

  public String getSecondaryUserAgent() {
    return secondaryUserAgent;
  }
}
