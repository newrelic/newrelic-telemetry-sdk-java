# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.3.2] - 2019-10-01
### Misc bugfixes and cleanup
- Update license format in source files
- Log when audit logging is enabled and endpoint url at startup
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

