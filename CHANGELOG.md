# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.17.0] 2025-07-10
- Add methods to the `Attributes` class: `containsKey`, `remove`

## [0.16.0] 2023-10-20
- Update Utils.generateUUID() to generate UUIDs conforming to RFC 4122 V4 by @tmancill in #300
- Updating okhttp dependency by @meiao in #311

## [0.15.0] 2022-06-15
- Update `BatchDataSender` to log the cause of `IOException`.
- Update to okhttp 4.10.0 to address [CVE-2020-29582](https://github.com/advisories/GHSA-cqj8-47ch-rvvq).

## [0.14.0] 2022-06-12
- Replace `UUID.randomUUID()` with a faster implementation.

## [0.13.2] 2022-05-26
- Update GSON library to address [CVE-2022-25647](https://github.com/advisories/GHSA-4jrv-ppp4-jm57)

## [0.13.1] 2022-01-06
- Fix bug introduced in `0.13.0` that broke the use of generics in the `MetricBuffer`.

## [0.13.0] 2022-01-06
- EU Endpoint Support added by [updating the SenderConfigurationBuilder API](https://github.com/newrelic/newrelic-telemetry-sdk-java/pull/276). 
- Includes endpoints that send Metric, Event, Log, and Span data to New Relic One.
- Added warnings to let users know if data exceeds some Ingest API limits. 

## [0.12.0] - 2021-02-19
- Accepts a New Relic APM license key as an alternative to an Insights Insert API key.

## [0.11.0] - 2021-01-26
- Merge `telemetry` module into `telemetry-core`
- Remove erroneous `module-info.class` from `telemetry-core` jar

## [0.10.0] - 2021-01-08
- Deprecated endpoint  and endpointWithPath APIs have been removed
- Add the notion of a notification handler to capture feedback from the TelemetryClient
- Adds a rate limiter on a how much telemetry can be scheduled to be sent
- Batch types are specified in relevant log messages

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

