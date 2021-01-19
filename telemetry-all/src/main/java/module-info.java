module com.newrelic.telemetry {
  exports com.newrelic.telemetry.core;
  exports com.newrelic.telemetry.core.metrics;
  exports com.newrelic.telemetry.core.events;
  exports com.newrelic.telemetry.core.http;
  exports com.newrelic.telemetry.client;
  exports com.newrelic.telemetry.javahttp;

  requires java.net.http;
  requires com.google.gson;
  requires org.slf4j;
}
