This module contains an implementation of the required http client interface for the SDK to function.

* `OkHttpPoster` : Implements the `HttpPoster` interface, using okhttp.

In addition, it has two builder shims for creating telemetry senders that use okhttp: 
`com.newrelic.telemetry.SimpleMetricBatchSender` and `com.newrelic.telemetry.SimpleSpanBatchSender`

See the `telemetry-examples` module for sample usage.

This library can be consumed via the following maven coordinates:

`com.newrelic.telemetry:telemetry-http-okhttp`