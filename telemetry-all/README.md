### Telemetry Combined Module

This module provides high-level APIs for sending telemetry to New Relic.

Produces a single JAR file with an automatic module name for use in modular
applications.

The expectation is that this will involve in time to become a fully modularized
build. As such, it has a minimum version of Java 11.

It includes the contents of the non-modular artifacts:

* telemetry-client
* telemetry-core
* telemetry-http-java11

It should be used instead of these artifacts in a modular build, and it cannot
coexist with them as a dependency.