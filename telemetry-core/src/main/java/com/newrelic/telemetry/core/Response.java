/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core;

/** The response (currently HTTP) from the ingest API. */
public final class Response {
  /** HTTP status code returned from the API call. */
  private final int statusCode;
  /** A human-friendly message for the status. */
  private final String statusMessage;
  /** Any body returned by the API call. */
  private final String body;

  public Response(int statusCode, String statusMessage, String body) {
    this.statusCode = statusCode;
    this.statusMessage = statusMessage;
    this.body = body;
  }

  public int getStatusCode() {
    return statusCode;
  }

  public String getStatusMessage() {
    return statusMessage;
  }

  public String getBody() {
    return body;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Response response = (Response) o;

    if (getStatusCode() != response.getStatusCode()) return false;
    if (getStatusMessage() != null
        ? !getStatusMessage().equals(response.getStatusMessage())
        : response.getStatusMessage() != null) return false;
    return getBody() != null ? getBody().equals(response.getBody()) : response.getBody() == null;
  }

  @Override
  public int hashCode() {
    int result = getStatusCode();
    result = 31 * result + (getStatusMessage() != null ? getStatusMessage().hashCode() : 0);
    result = 31 * result + (getBody() != null ? getBody().hashCode() : 0);
    return result;
  }

  @Override
  public String toString() {
    return "Response{"
        + "statusCode="
        + statusCode
        + ", statusMessage='"
        + statusMessage
        + '\''
        + ", body='"
        + body
        + '\''
        + '}';
  }
}
