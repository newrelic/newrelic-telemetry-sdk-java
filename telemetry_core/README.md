### Telemetry Core Module

This module contains the low-level APIs to send dimensional metrics and spans to New Relic.

Javadoc can be found here: [![Javadocs][javadoc-image]][javadoc-url]

Since it is intended to have minimal dependencies, it requires an HTTP implementation
in order to function. Implementation of the following interface is required:

`HttpPoster` : a very simple interface for sending an HTTP Post and returning a response.
A reference implementation based on `okhttp` is provided in the `telemetry-http-okhttp` module.

If you want to consume this module as-is, it is published at the maven coordinate:

`com.newrelic.telemetry:metrics`

[javadoc-image]: https://www.javadoc.io/badge/com.newrelic.telemetry/telemetry-core.svg
[javadoc-url]: https://www.javadoc.io/doc/com.newrelic.telemetry/telemetry-core