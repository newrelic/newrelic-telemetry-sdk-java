package com.newrelic.telemetry;

import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.util.Utils;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public abstract class AbstractSenderBuilder<B extends AbstractSenderBuilder<B>> {

  protected String apiKey;
  protected HttpPoster httpPoster;
  protected URL sendUrl;
  protected boolean auditLoggingEnabled = false;
  protected String secondaryUserAgent;

  protected URL getOrDefaultSendUrl() {
    if (sendUrl != null) {
      return sendUrl;
    }
    try {
      return constructUrlWithHost(URI.create(getDefaultUrl()));
    } catch (MalformedURLException e) {
      throw new UncheckedIOException("Bad hardcoded URL", e);
    }
  }

  protected abstract String getDefaultUrl();

  protected abstract String getBasePath();

  protected URL constructUrlWithHost(URI hostUri) throws MalformedURLException {
    return hostUri.resolve(getBasePath()).toURL();
  }

  /**
   * Set a URI to override the default ingest endpoint.
   *
   * @param uriOverride The scheme, host, and port that should be used for the Spans API endpoint.
   *     The path component of this parameter is unused.
   * @return the Builder
   * @throws MalformedURLException This is thrown when the provided URI is malformed.
   */
  public B uriOverride(URI uriOverride) throws MalformedURLException {
    sendUrl = constructUrlWithHost(uriOverride);
    return self();
  }

  public abstract Object build();

  @SuppressWarnings("unchecked")
  protected final B self() {
    return (B) this;
  }

  /**
   * Turns on audit logging. Payloads sent will be logged at the DEBUG level. Please note that if
   * your payloads contain sensitive information, that information will be logged wherever your logs
   * are configured.
   */
  public B enableAuditLogging() {
    this.auditLoggingEnabled = true;
    return self();
  }

  /**
   * Provide your New Relic Insights Insert API key
   *
   * @see <a
   *     href="https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key">New
   *     Relic API Keys</a>
   */
  public B apiKey(String apiKey) {
    this.apiKey = apiKey;
    return self();
  }

  /**
   * Provide an implementation for HTTP POST. {@link #build()} will throw if an implementation is
   * not provided or this method is not called.
   */
  public B httpPoster(HttpPoster httpPoster) {
    this.httpPoster = httpPoster;
    return self();
  }

  /**
   * By default, the DEFAULT_URL defined in specific subclasses is used. Otherwise uses the provided
   * {@code traceUrl}
   *
   * @deprecated Use the {@link #uriOverride(URI)} method instead.
   */
  public B traceUrl(URL traceUrl) {
    sendUrl = traceUrl;
    return self();
  }

  /**
   * Provide additional user agent information. The product is required to be non-null and
   * non-empty. The version is optional, although highly recommended.
   */
  public B secondaryUserAgent(String product, String version) {
    Utils.verifyNonBlank(product, "Product cannot be null or empty in the secondary user-agent.");
    if (version == null || version.isEmpty()) {
      secondaryUserAgent = product;
    } else {
      secondaryUserAgent = product + "/" + version;
    }
    return self();
  }
}
