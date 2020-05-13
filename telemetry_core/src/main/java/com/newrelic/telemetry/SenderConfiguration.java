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

/** Configuration options for the various classes that send data to the New Relic ingest APIs. */
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

    /**
     * Configure the New Relic Insert API key to use.
     *
     * @return this builder;
     */
    public SenderConfigurationBuilder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    public SenderConfigurationBuilder httpPoster(HttpPoster httpPoster) {
      this.httpPoster = httpPoster;
      return this;
    }

    /**
     * Configure the *full* endpoint URL for data to be sent to, including the path.
     *
     * @param endpointUrl A full {@link URL}, including the path.
     * @return this builder.
     */
    public SenderConfigurationBuilder endpointWithPath(URL endpointUrl) {
      this.endpointUrl = endpointUrl;
      return this;
    }

    /**
     * Configure the endpoint for data to be sent to. The default path will be used.
     *
     * @param scheme A valid URL scheme, such as "https"
     * @param host The host portion of the URL.
     * @param port The port portion of the URL.
     * @return this builder.
     * @throws MalformedURLException If a valid URL cannot be constructed from the pieces provided.
     */
    public SenderConfigurationBuilder endpoint(String scheme, String host, int port)
        throws MalformedURLException {
      return endpointWithPath(new URL(scheme, host, port, basePath));
    }

    /**
     * Configure whether audit logging is enabled. Note: audit logging will log all data payloads
     * sent to New Relic at DEBUG level, in plain text.
     *
     * @return this builder.
     */
    public SenderConfigurationBuilder auditLoggingEnabled(boolean auditLoggingEnabled) {
      this.auditLoggingEnabled = auditLoggingEnabled;
      return this;
    }

    /**
     * Configure a secondary User-Agent value to use when sending data. This will be appended to the
     * default User-Agent that the SDK sends, and is useful for monitoring various sources of data
     * coming into the New Relic systems.
     *
     * @return this builder.
     */
    public SenderConfigurationBuilder secondaryUserAgent(String secondaryUserAgent) {
      this.secondaryUserAgent = secondaryUserAgent;
      return this;
    }

    public SenderConfiguration build() {
      return new SenderConfiguration(
          apiKey, httpPoster, getOrDefaultSendUrl(), auditLoggingEnabled, secondaryUserAgent);
    }

    private URL getOrDefaultSendUrl() {
      try {
        if (endpointUrl != null) {
          return endpointUrl;
        }
        return constructUrlWithHost(URI.create(defaultUrl));
      } catch (MalformedURLException e) {
        throw new UncheckedIOException("Bad Hardcoded URL " + defaultUrl, e);
      }
    }

    public URL constructUrlWithHost(URI hostUri) throws MalformedURLException {
      return hostUri.resolve(basePath).toURL();
    }
  }
}
