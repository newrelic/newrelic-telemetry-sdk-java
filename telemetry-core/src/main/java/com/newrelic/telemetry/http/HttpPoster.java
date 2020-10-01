/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.http;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * In order to provide your own implementation of an HTTP client, an implementation of this
 * interface must be provided.
 */
public interface HttpPoster {

  /** Post data to the provided URL. */
  HttpResponse post(URL url, Map<String, String> headers, byte[] body, String mediaType)
      throws IOException;
}
