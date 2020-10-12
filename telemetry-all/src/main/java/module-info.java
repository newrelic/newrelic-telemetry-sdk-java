module com.newrelic.telemetry {
  exports com.newrelic.telemetry;
  exports com.newrelic.telemetry.metrics;
  exports com.newrelic.telemetry.events;
  exports com.newrelic.telemetry.http;

  requires java.net.http;
  requires com.google.gson;
  requires org.slf4j;
}
