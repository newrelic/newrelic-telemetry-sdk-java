## Telemetry Examples

This module contains a set of examples on how to use the telemetry SDK. 
They are full examples, which should compile and be runnable as-is. 

You will need to provide your Insights Insert Key or License Key and the New Relic Metric API endpoint as command-line parameters to the programs.

### The examples

#### [GaugeExample.java](src/main/java/com/newrelic/telemetry/examples/GaugeExample.java)

This is an example of how to write Gauge metrics to New Relic using the SDK.

#### [SummaryExample.java](src/main/java/com/newrelic/telemetry/examples/SummaryExample.java)  

This is an example of how to write Summary metrics to New Relic using the SDK.

#### [CountExample.java](src/main/java/com/newrelic/telemetry/examples/CountExample.java)

This is an example of how to write Count metrics to New Relic using the SDK.

#### [BoundaryExample.java](src/main/java/com/newrelic/telemetry/examples/BoundaryExample.java)

This is an example of pushing the boundaries of what is accepted by the New Relic APIs,
and how you can see what happens when things go wrong.

#### [SpanExample.java](src/main/java/com/newrelic/telemetry/examples/SpanExample.java)

This example shows you how you can send spans to the New Relic trace ingest api.
It demonstrates how to use the `SimpleSpanBatchSender` to easily create a `SpanBatchSender`
that can be fed a `SpanBatch`.

#### [SpanToTraceObserverExample.java](src/main/java/com/newrelic/telemetry/examples/SpanToTraceObserverExample.java)

This example shows you how you can send spans to an alternate endpoint.  In this case, we are sending to a provisioned TraceObserver for Infinite Tracing on the New Relic Edge. 
Sending spans to a TraceObserver requires a modification to the default behavior. The default behavior is to send data to the Portland production US endpoints.
The example demonstrates how to use the `SpanBatchSenderFactory` to easily create a `SpanBatchSender` and change the endpoint by providing the URL of 
a provisioned TraceObserver.  
The expected TraceObserver URL format is given below.

`https://<Your-TraceObserver-UUID>.aws-us-east-1.tracing.edge.nr-data.net:443/trace/v1`
 
For more information on TraceObserver and Infinite Tracing [go here.](https://docs.newrelic.com/docs/understand-dependencies/distributed-tracing/enable-configure/language-agents-enable-distributed-tracing#provision-trace-observer)


#### [TelemetryClientExample.java](src/main/java/com/newrelic/telemetry/examples/TelemetryClientExample.java)

This is an example of how to use the provided `com.newrelic.telemetry.TelemetryClient` to handle
errors in the recommended way.

### Running the examples

You can build the examples using gradle tasks.  You'll need your [New Relic Insights Insert API Key](https://docs.newrelic.com/docs/insights/insights-data-sources/custom-data/introduction-event-api#register) or [New Relic APM License Key](https://docs.newrelic.com/docs/accounts/accounts-billing/account-setup/new-relic-license-key/).
Provide the Insights Key as the `-PapiKey=<your-api-key>` property and run the gradle task in the usual way, as shown below.

The command below will run the `BoundaryExample` class using your Insights key.

`./gradlew telemetry_examples:BoundaryExample -PapiKey=<Your Insert API Key>`

To run the `BoundaryExample` with your license key, you'll modify `MetricBatchSender` with an additional `useLicenseKey` builder configuration.

```java
MetricBatchSender.create(factory.configureWith(insightsInsertKey).useLicenseKey(true).build());
```
You will then run this command.

`./gradlew telemetry_examples:BoundaryExample -PapiKey=<Your License Key>`

The `SpanToTraceObserverExample` requires two arguments.

`./gradlew telemetry_examples:SpanToTraceObserverExample --args=<'Your Insert API Key> <https://<Your-TraceObserver-UUID>.aws-us-east-1.tracing.edge.nr-data.net:443/trace/v1'>`