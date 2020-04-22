/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.newrelic.telemetry;

import com.newrelic.telemetry.http.HttpPoster;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class SenderConfiguration {
  private final String apiKey;
  private final HttpPoster httpPoster;
  private final URL endpointUrl;
  private final boolean auditLoggingEnabled;
  private final String secondaryUserAgent;

  public SenderConfiguration(
      String apiKey,
      HttpPoster httpPoster,
      URL endpointUrl,
      boolean auditLoggingEnabled,
      String secondaryUserAgent) {
    this.apiKey = apiKey;
    this.httpPoster = httpPoster;
    this.endpointUrl = endpointUrl;
    this.auditLoggingEnabled = auditLoggingEnabled;
    this.secondaryUserAgent = secondaryUserAgent;
  }

  public String getApiKey() {
    return apiKey;
  }

  public HttpPoster getHttpPoster() {
    return httpPoster;
  }

  public URL getEndpointUrl() {
    return endpointUrl;
  }

  public boolean isAuditLoggingEnabled() {
    return auditLoggingEnabled;
  }

  public String getSecondaryUserAgent() {
    return secondaryUserAgent;
  }

  public static SenderConfigurationBuilder builder(String defaultUrl, String basePath) {
    return new SenderConfigurationBuilder(defaultUrl, basePath);
  }

  public static class SenderConfigurationBuilder {

    private final String defaultUrl;
    private final String basePath;

    private String apiKey;
    private HttpPoster httpPoster;
    private URL endpointUrl;
    private boolean auditLoggingEnabled = false;
    private String secondaryUserAgent;

    public SenderConfigurationBuilder(String defaultUrl, String basePath) {
      this.defaultUrl = defaultUrl;
      this.basePath = basePath;
    }

    public SenderConfigurationBuilder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public SenderConfigurationBuilder httpPoster(HttpPoster httpPoster) {
      this.httpPoster = httpPoster;
      return this;
    }

    public SenderConfigurationBuilder endpointUrl(URL sendUrl) {
      this.endpointUrl = sendUrl;
      return this;
    }

    public SenderConfigurationBuilder auditLoggingEnabled(boolean auditLoggingEnabled) {
      this.auditLoggingEnabled = auditLoggingEnabled;
      return this;
    }

    public SenderConfigurationBuilder secondaryUserAgent(String secondaryUserAgent) {
      this.secondaryUserAgent = secondaryUserAgent;
      return this;
    }

    public SenderConfiguration build() {
      return new SenderConfiguration(
          apiKey, httpPoster, getOrDefaultSendUrl(), auditLoggingEnabled, secondaryUserAgent);
    }

    private URL getOrDefaultSendUrl() {
      if (endpointUrl != null) {
        return endpointUrl;
      }
      try {
        return constructUrlWithHost(URI.create(defaultUrl));
      } catch (MalformedURLException e) {
        throw new UncheckedIOException("Bad hardcoded URL", e);
      }
    }

    protected URL constructUrlWithHost(URI hostUri) throws MalformedURLException {
      return hostUri.resolve(basePath).toURL();
    }
  }
}
