# New Relic Java Telemetry SDK
The New Relic Java Telemetry SDK for sending dimensional metrics to New Relic.

### Module structure:

#### `metrics`
This is the core module for sending dimensional metrics to New Relic. The library is published under maven coordinates:

`com.newrelic.telemetry:metrics`

Note: in order to use these APIs, you will need to get access to the API endpoint. 
Please contact `open-instrumentation@newrelic.com` to request access.

You will also need an Insights Insert API Key. 
Please see [New Relic Api Keys](https://docs.newrelic.com/docs/apis/getting-started/intro-apis/understand-new-relic-api-keys#user-api-key)
for more information.

#### `telemetry`
This module contains code for using all New Relic telemetry modules, gathered in one place, as well as what we 
consider "best practice" implementations of how to interact with the lower-level modules.

#### `telemetry_components`
This is additional components that are useful for using the SDK. It contains reference implementations for
required components, implemented using standard open source libraries. 
The `telemetry-components` library is published under the maven coordinates:

`com.newrelic.telemetry:telemetry-components`

#### `metrics_examples`
Example code for using the metrics API.

#### `integration_test`
Integration test module. Uses docker-compose based tests to test the SDK end-to-end.

### Prerequisites

* Java 8
* For IDEA:
    * lombok plugin installed
    * annotation processing enabled for the project
* Docker & docker-compose must be installed for integration testing

### Integration Testing

This is done with the docker-compose gradle plugin, with [mock-server](https://github.com/jamesdbloom/mockserver) providing the backend

There are two modes to run the integration tests.
1. Run with gradle: `$ ./gradlew integration_test:test`
2. Start up the mock server with `$ docker-compose up`, then run the `LowLevelApiIntegrationTest` class in IDEA.


### Code style
This project uses the [google-java-format](https://github.com/google/google-java-format) code style, and it is 
easily applied via an included [gradle plugin](https://github.com/sherter/google-java-format-gradle-plugin):

`$ ./gradlew googleJavaFormat verifyGoogleJavaFormat`

Please be sure to run the formatter before committing any changes. There is a `pre-commit-hook.sh` which can 
be applied automatically before commits by moving it into `.git/hooks/pre-commit`.
