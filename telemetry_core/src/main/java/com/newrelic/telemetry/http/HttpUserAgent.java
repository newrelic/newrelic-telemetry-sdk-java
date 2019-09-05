package com.newrelic.telemetry.http;

import com.newrelic.telemetry.util.Utils;
import java.util.Optional;

public class HttpUserAgent {

  public static final String VALUE;
  static {
    Package thisPackage = Utils.class.getPackage();
    String implementationVersion =
        Optional.ofNullable(thisPackage.getImplementationVersion()).orElse("Unknown Version");
    VALUE = "NewRelic-Java-TelemetrySDK/" + implementationVersion;
  }

}
