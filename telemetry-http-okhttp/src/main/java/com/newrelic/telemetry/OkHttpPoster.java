/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.http.HttpResponse;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Map;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/** Implementation of the HttpPoster interface using an OkHttp client. */
public class OkHttpPoster implements HttpPoster {
  private final OkHttpClient okHttpClient;

  /** Create an OkHttpPoster with a default OkHttpClient, with only a custom call timeout. */
  public OkHttpPoster(Duration callTimeout) {
    this(new OkHttpClient.Builder().callTimeout(callTimeout).build());
  }

  /** Create an OkHttpPoster with your own OkHttpClient implementation. */
  public OkHttpPoster(OkHttpClient client) {
    this.okHttpClient = client;
  }

  @Override
  public HttpResponse post(URL url, Map<String, String> headers, byte[] body, String mediaType)
      throws IOException {
    RequestBody requestBody = RequestBody.create(MediaType.get(mediaType), body);
    Request request =
        new Request.Builder().url(url).headers(Headers.of(headers)).post(requestBody).build();
    try (okhttp3.Response response = okHttpClient.newCall(request).execute()) {
      return new HttpResponse(
          response.body() != null ? response.body().string() : null,
          response.code(),
          response.message(),
          response.headers().toMultimap());
    }
  }

  public OkHttpClient getOkHttpClient() {
    return okHttpClient;
  }
}
