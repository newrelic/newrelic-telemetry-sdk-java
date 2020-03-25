package com.newrelic.telemetry;

import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.http.HttpResponse;
import java.io.IOException;
import java.net.URL;
import java.net.http.HttpClient;
import java.util.Map;

/** Implementation of the HttpPoster interface using an Java 11 JDK Http client. */
public class Java11HttpPoster implements HttpPoster {
  private final HttpClient httpClient;

  /** Create a Java11HttpPoster with your own object. */
  public Java11HttpPoster(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  @Override
  public HttpResponse post(URL url, Map<String, String> headers, byte[] body, String mediaType)
      throws IOException {
    return null;
  }
}
