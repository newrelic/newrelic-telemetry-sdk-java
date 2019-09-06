### Telemetry Module

This module provides high-level APIs for sending telemetry to New Relic.

See the `telemetry-examples` module for sample usage.

This library can be consumed via the following maven coordinates:

`com.newrelic.telemetry:telemetry`

Since the underlying code that this depends on requires implementations for an HTTP client, 
you can get our reference implementations of the `HttpPoster` interface
from the `telemetry-http-okhttp` module and library:

`com.newrelic.telemetry:telemetry-http-okhttp`
