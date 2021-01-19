/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.core.http;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

/**
 * In order to provide your own implementation of an HTTP client, an implementation of this
 * interface must be provided.
 */
public interface HttpPoster {

  /**
   * Post data to the provided URL.
   *
   * @param url http url to be reached
   * @param headers headers to be sent
   * @param body body to be sent
   * @param mediaType media type definition
   * @return http response from the POST request
   * @throws IOException in case of http request error
   */
  HttpResponse post(URL url, Map<String, String> headers, byte[] body, String mediaType)
      throws IOException;
}
