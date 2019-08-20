This module contains reference implementations of the required interfaces for the SDK to function.

* `MetricGsonGenerator` : Implements the `MetricJsonGenerator` interface, using gson.
* `OkHttpPoster` : Implements the `HttpPoster` interface, using okhttp.

In addition, it has a builder shim that uses these called `SimpleMetricBatchSender`

See the `telemetry-examples` module for sample usage.

This library can be consumed via the following maven coordinates:

`com.newrelic.telemetry:telemetry-components`