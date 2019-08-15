/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.http;

import java.util.List;
import java.util.Map;
import lombok.Value;

/** The response from the backend APIs. */
@Value
public class HttpResponse {
  String body;
  int code;
  String message;
  Map<String, List<String>> headers;
}
