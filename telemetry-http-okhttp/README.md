This module contains reference implementations of the required http client for the SDK to function.

* `OkHttpPoster` : Implements the `HttpPoster` interface, using okhttp.

In addition, it has a builder shim that uses these called `SimpleMetricBatchSender`

See the `telemetry-examples` module for sample usage.

This library can be consumed via the following maven coordinates:

`com.newrelic.telemetry:telemetry-http-okhttp`