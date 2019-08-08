This module contains reference implementations of the required interfaces for the SDK for function.

* `MetricGsonGenerator` : Implements the `MetricJsonGenerator` interface, using gson.
* `OkHttpPoster` : Implements the `HttpPoster` interface, using okhttp.

In addition, it has a builder shim that uses these called `SimpleMetricBatchSender`

TODO: samples of using these things.