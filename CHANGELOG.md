# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## Coming soon
- Remove several deprecated APIs.

## [0.9.0] - 2020-10-23
- Fixed telemetry-all to be a modular java build.
- Improve javadocs.

## [0.8.0] - 2020-09-29
- CRITICAL: Fix default ingest URI for event data. Upgrade strongly recommended.
- Size of `MetricBuffer` [is now exposed via `size()` method](https://github.com/newrelic/newrelic-telemetry-sdk-java/pull/215).
- [x-request-id header is now included](https://github.com/newrelic/newrelic-telemetry-sdk-java/pull/218) on data ingest http requests, and `TelemetryBatch` instances each have a UUID.
- Cleaner, [less verbose shutdown](https://github.com/newrelic/newrelic-telemetry-sdk-java/pull/220).
- Events now [disallow null and empty event types](https://github.com/newrelic/newrelic-telemetry-sdk-java/pull/221).
- When audit logging is enabled, dropped metrics [will now be logged](https://github.com/newrelic/newrelic-telemetry-sdk-java/pull/222) for troubleshooting.
- We now have [an example](https://github.com/newrelic/newrelic-telemetry-sdk-java/blob/main/telemetry_examples/src/main/java/com/newrelic/telemetry/examples/SpanToTraceObserverExample.java) of how to send spans to an Infinite Tracing trace observer.


## [0.7.0] - 2020-07-27
- Quieter logging when IOExceptions occur during data transmission.
- Improve accuracy of version handling when bundled with other software (onejar/shadowjar)
- Add `.endpoint(url)` to the `SenderConfiguration` and deprecate the others that don't take a fully qualified URL.
- Better support for SLF4J with Java 11 modules system
- Add simple one-shot factory methods `OkHttp.newTelemetryClient()` and `Java11Http.newTelemetryClient()`
- Upgrade to OkHttp 4.8.0 (latest)  

## [0.6.1] - 2020-06-18
- Fix the default metric API URL to point at the metric API

## [0.6.0] - 2020-05-28
- Add initial preliminary support for Logs data type
- Simplified creation TelemetryClient and friends
- Remove hard gson dependency in a way that is compatible with other versions of gson

## [0.5.1] - 2020-04-30
- Restore methods that were deleted from deprecated classes.

## [0.5.0] - 2020-04-30
- Add support for Events data type
- Add Java 11 HTTP provider
- Add javadoc.io
- Use Gradle shadowing to remove C&P GSON code (Thanks wpoch)
- Remove Lombok dependency
- Miscellaneous cleanups/enhancements

## [0.4.0] - 2020-03-04
- Additional documentation for logging
- Improved details in log messages, including number of metrics dropped
- Add first-class support for `service.name` and `instrumentation.provider` via `MetricBatch.Builder`
- Add incremental retry with backoff strategy that will eventually time out and give up
- Miscellaneous cleanups/enhancements 
 
## [0.3.4] - 2020-01-06
- Allow adding user-supplied suffix to HTTP `User-Agent`

## [0.3.3] - 2019-12-18
### Misc bugfixes and cleanup
- Allow summary min/max to be null 

## [0.3.2] - 2019-10-01
### Misc bugfixes and cleanup
- Update license format in source files
- At startup, log something when audit logging is enabled
- At startup, log endpoint url
- Fix for [bug #102](https://github.com/newrelic/newrelic-telemetry-sdk-java/issues/102) - nulls sent in spans json payload

## [0.3.1] - 2019-09-09
### Misc bugfixes and cleanup
- Adds `Implementation-Version` and `Implementation-Vendor` to jar manifests
- Properly escapes string in the span json
- Set the proper version in the `User-Agent` string sent to the backend APIs
- Fixes a stack overflow bug in the SimpleSpanBatchSender
- Adds `error` as a top-level attribute in the Span

## [0.3.0] - 2019-09-06
### Support for traces via spans
- Remove Gson as a dependency
- Support for sending spans to the New Relic trace API.
- Renamed telemetry-components module to telemetry-http-okhttp
- Renamed metrics module to telemetry-core
- This release contains API changes that are not backwards compatible.

## [0.2.1] - 2019-08-26
### Initial public release of the SDK
- Support for sending dimensional metrics to New Relic.
- Reference implementations of the `HttpPoster` and `MetricToJson` using `okhttp` and `gson` respectively.

