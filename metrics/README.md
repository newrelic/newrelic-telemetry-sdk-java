### Metrics Module

This module contains the low-level APIs to send dimensional metric data to New Relic.

Since it is intended to have minimal dependencies, it requires an HTTP implementation
and a JSON implementation in order to function. Implementations of two interfaces
are required:

`HttpPoster` : a very simple interface for sending an HTTP Post and returning a response.
A reference implementation based on `okhttp` is provided in the `telemetry-components` module.

`MetricJsonGenerator` : an interface for generating the various pieces of the JSON for metric data.
A reference implementation based on `gson` is provided in the `telemetry-components` module.

If you want to consume this module as-is, it is published at hte maven coordinate:

`com.newrelic.telemetry:metrics`

