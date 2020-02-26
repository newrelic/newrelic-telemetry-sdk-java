package com.newrelic.telemetry.events;

import com.newrelic.telemetry.events.json.EventBatchMarshaller;
import com.newrelic.telemetry.http.HttpPoster;
import com.newrelic.telemetry.json.AttributesJson;
import com.newrelic.telemetry.metrics.MetricBatchSender;
import com.newrelic.telemetry.metrics.json.MetricBatchJsonCommonBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricBatchJsonTelemetryBlockWriter;
import com.newrelic.telemetry.metrics.json.MetricBatchMarshaller;
import com.newrelic.telemetry.metrics.json.MetricToJson;
import com.newrelic.telemetry.transport.BatchDataSender;
import com.newrelic.telemetry.util.Utils;

import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

public class EventBatchSenderBuilder {

    private static final String eventsPath = "/metric/v1";

    private String apiKey;
    private HttpPoster httpPoster;
    private URL metricsUrl;
    private boolean auditLoggingEnabled = false;
    private String secondaryUserAgent;

    public EventBatchSender build() {
        Utils.verifyNonNull(apiKey, "API key cannot be null");
        Utils.verifyNonNull(httpPoster, "an HttpPoster implementation is required.");

        URL url = getOrDefaultMetricsUrl();

        EventBatchMarshaller marshaller =
                new EventBatchMarshaller();
//                        new MetricBatchJsonCommonBlockWriter(new AttributesJson()),
//                        new MetricBatchJsonTelemetryBlockWriter(new MetricToJson()));
        BatchDataSender sender =
                new BatchDataSender(httpPoster, apiKey, url, auditLoggingEnabled, secondaryUserAgent);

        return new EventBatchSender(marshaller, sender);
    }

    private URL getOrDefaultMetricsUrl() {
        if (metricsUrl != null) {
            return metricsUrl;
        }
        try {
            return constructMetricsUrlWithHost(URI.create("https://metric-api.newrelic.com/"));
        } catch (MalformedURLException e) {
            throw new UncheckedIOException("Bad hardcoded URL", e);
        }
    }

    private static URL constructMetricsUrlWithHost(URI hostUri) throws MalformedURLException {
        return hostUri.resolve(eventsPath).toURL();
    }


}
