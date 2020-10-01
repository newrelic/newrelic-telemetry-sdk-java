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
  private final BaseConfig baseConfig;
  private final HttpPoster httpPoster;
  private final URL endpointUrl;

  public SenderConfiguration(
      String apiKey,
      HttpPoster httpPoster,
      URL endpointUrl,
      boolean auditLoggingEnabled,
      String secondaryUserAgent) {
    this.httpPoster = httpPoster;
    this.endpointUrl = endpointUrl;
    this.baseConfig = new BaseConfig(apiKey, auditLoggingEnabled, secondaryUserAgent);
  }

  public String getApiKey() {
    return baseConfig.getApiKey();
  }

  public HttpPoster getHttpPoster() {
    return httpPoster;
  }

  public URL getEndpointUrl() {
    return endpointUrl;
  }

  public boolean isAuditLoggingEnabled() {
    return baseConfig.isAuditLoggingEnabled();
  }

  public String getSecondaryUserAgent() {
    return baseConfig.getSecondaryUserAgent();
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
     * Configure the endpoint for data to be sent to. The default path will be used.
     *
     * <p>To be removed in 0.8.0
     *
     * @param scheme A valid URL scheme, such as "https"
     * @param host The host portion of the URL.
     * @param port The port portion of the URL.
     * @return this builder.
     * @throws MalformedURLException If a valid URL cannot be constructed from the pieces provided.
     * @deprecated call the simpler endpoint(URL) method with the full URL instead
     */
    @Deprecated
    public SenderConfigurationBuilder endpoint(String scheme, String host, int port)
        throws MalformedURLException {
      return endpointWithPath(new URL(scheme, host, port, basePath));
    }

    /**
     * Configure the *full* endpoint URL for data to be sent to, including the path.
     *
     * <p>To be removed in 0.8.0
     *
     * @deprecated call the simpler endpoint() method instead
     * @param endpointUrl A full {@link URL}, including the path.
     * @return this builder.
     */
    @Deprecated
    public SenderConfigurationBuilder endpointWithPath(URL endpointUrl) {
      return endpoint(endpointUrl);
    }

    /**
     * Configure the *full* endpoint URL for data to be sent to, including the path. You should only
     * use this method if you wish to modify the default behavior, which is to send data to the
     * Portland production US endpoints.
     *
     * @param endpoint A full {@link URL}, including the path.
     * @return this builder.
     */
    public SenderConfigurationBuilder endpoint(URL endpoint) {
      this.endpointUrl = endpoint;
      return this;
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
