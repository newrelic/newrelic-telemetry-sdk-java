/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry;

import lombok.Value;

/** The response (currently HTTP) from the ingest API. */
@Value
public class Response {
  /** HTTP status code returned from the API call. */
  int statusCode;
  /** A human-friendly message for the status. */
  String statusMessage;
  /** Any body returned by the API call. */
  String body;
}
