### Telemetry Module

This module provides high-level APIs for sending telemetry to New Relic.

See the `telemetry-examples` module for sample usage.

This library can be consumed via the following maven coordinates:

`com.newrelic.telemetry:telemetry`

Since the underlying code that this depends on requires implementations for HTTP and JSON, 
you can get our reference implementations of the `HttpPoster` and `MetricToJson` interfaces
from the `telemetry-components` module and library:

`com.newrelic.telemetry:telemetry-components`
