### Metrics Module

This module contains the low-level APIs to send dimensional metric data to New Relic.

Since it is intended to have minimal dependencies, it requires an HTTP implementation
in order to function. Implementations of an interface is required:

`HttpPoster` : a very simple interface for sending an HTTP Post and returning a response.
A reference implementation based on `okhttp` is provided in the `telemetry-components` module.

If you want to consume this module as-is, it is published at hte maven coordinate:

`com.newrelic.telemetry:metrics`

