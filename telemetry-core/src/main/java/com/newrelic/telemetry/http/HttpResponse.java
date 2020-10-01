/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.http;

import java.util.List;
import java.util.Map;

/** The response from the backend APIs. */
public final class HttpResponse {
  private final String body;
  private final int code;
  private final String message;
  private final Map<String, List<String>> headers;

  public HttpResponse(String body, int code, String message, Map<String, List<String>> headers) {
    this.body = body;
    this.code = code;
    this.message = message;
    this.headers = headers;
  }

  public String getBody() {
    return body;
  }

  public int getCode() {
    return code;
  }

  public String getMessage() {
    return message;
  }

  public Map<String, List<String>> getHeaders() {
    return headers;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    HttpResponse that = (HttpResponse) o;

    if (getCode() != that.getCode()) return false;
    if (getBody() != null ? !getBody().equals(that.getBody()) : that.getBody() != null)
      return false;
    if (getMessage() != null ? !getMessage().equals(that.getMessage()) : that.getMessage() != null)
      return false;
    return getHeaders() != null
        ? getHeaders().equals(that.getHeaders())
        : that.getHeaders() == null;
  }

  @Override
  public int hashCode() {
    int result = getBody() != null ? getBody().hashCode() : 0;
    result = 31 * result + getCode();
    result = 31 * result + (getMessage() != null ? getMessage().hashCode() : 0);
    result = 31 * result + (getHeaders() != null ? getHeaders().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "HttpResponse{"
        + "body='"
        + body
        + '\''
        + ", code="
        + code
        + ", message='"
        + message
        + '\''
        + ", headers="
        + headers
        + '}';
  }
}
