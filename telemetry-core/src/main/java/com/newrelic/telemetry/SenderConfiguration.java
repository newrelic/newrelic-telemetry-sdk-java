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
  private final boolean useLicenseKey;

  public static String endpointRegion = "US";


  public SenderConfiguration(
      String apiKey,
      HttpPoster httpPoster,
      URL endpointUrl,
      boolean auditLoggingEnabled,
      String secondaryUserAgent) {
    this(apiKey, httpPoster, endpointUrl, auditLoggingEnabled, secondaryUserAgent, false);
  }

  public SenderConfiguration(
      String apiKey,
      HttpPoster httpPoster,
      URL endpointUrl,
      boolean auditLoggingEnabled,
      String secondaryUserAgent,
      boolean useLicenseKey) {
    this.httpPoster = httpPoster;
    this.endpointUrl = endpointUrl;
    this.baseConfig = new BaseConfig(apiKey, auditLoggingEnabled, secondaryUserAgent);
    this.useLicenseKey = useLicenseKey;
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

  public boolean useLicenseKey() {
    return useLicenseKey;
  }

  public String getRegion() { return endpointRegion; }


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
    private boolean useLicenseKey = false;
    private String secondaryUserAgent;

    public SenderConfigurationBuilder(String defaultUrl, String basePath) {
      this.defaultUrl = defaultUrl;
      this.basePath = basePath;
    }

    /**
     * Configure the New Relic Insert API key to use.
     *
     * @param apiKey new relic api key
     * @return this builder;
     */
    public SenderConfigurationBuilder apiKey(String apiKey) {
      this.apiKey = apiKey;
      return this;
    }

    /**
     * @param httpPoster client responsible to execute the http request
     * @return this builder
     */
    public SenderConfigurationBuilder httpPoster(HttpPoster httpPoster) {
      this.httpPoster = httpPoster;
      return this;
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
     * @param useLicenseKey flag to indicate the configured {@code apiKey} is a license key, not an
     *     insights api key
     * @return this builder
     */
    public SenderConfigurationBuilder useLicenseKey(boolean useLicenseKey) {
      this.useLicenseKey = useLicenseKey;
      return this;
    }

    /**
     * Sets the region so that it is used in the individual batch senders (e.x. MetricBatchSender,
     * LogBatchSender, SpanBatchSenders) to configure regional endpoints and send data to New Relic
     *
     * @param region String to indicate whether the account is in an American (US) or European (EU) region
     * @return this builder
     *
     */

    public SenderConfigurationBuilder setRegion(String region) {
      SenderConfiguration.endpointRegion = region.toUpperCase();
      return this;
    }

    /**
     * Configure whether audit logging is enabled. Note: audit logging will log all data payloads
     * sent to New Relic at DEBUG level, in plain text.
     *
     * @param auditLoggingEnabled if true, audit log will be enabled
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
     * @param secondaryUserAgent configure a secondary User-Agent
     * @return this builder.
     */
    public SenderConfigurationBuilder secondaryUserAgent(String secondaryUserAgent) {
      this.secondaryUserAgent = secondaryUserAgent;
      return this;
    }

    public SenderConfiguration build() {
      return new SenderConfiguration(
          apiKey,
          httpPoster,
          getOrDefaultSendUrl(),
          auditLoggingEnabled,
          secondaryUserAgent,
          useLicenseKey);
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
