/*
 * Copyright 2019 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */
package com.newrelic.telemetry.examples;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.MetricBatchSenderFactory;
import com.newrelic.telemetry.OkHttpPoster;
import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.exceptions.ResponseException;
import com.newrelic.telemetry.metrics.Count;
import com.newrelic.telemetry.metrics.MetricBatchSender;
import com.newrelic.telemetry.metrics.MetricBuffer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/**
 * The purpose of this example is demonstrate some of the boundaries of the New Relic Metric API,
 * and exercise those boundaries. When metrics are invalid, for whatever reason, a
 * "NrIntegrationError" custom event will be created in your account in Insights with your API Key
 * and the requestId as custom attributes.
 *
 * <p>The exact response of the Metric API is not guaranteed for any of these edge cases. They are
 * just provided as example of things that might cause issues.
 *
 * <p>To run this example, provide a command line argument for your Insights Insert key.
 */
public class BoundaryExample {
  private static final Logger logger = Logger.getLogger(BoundaryExample.class.getName());

  public static void main(String[] args) throws ResponseException {
    String licenseKey = args[0];

    MetricBatchSenderFactory factory =
        MetricBatchSenderFactory.fromHttpImplementation(OkHttpPoster::new);
    MetricBatchSender sender =
        MetricBatchSender.create(factory.configureWith(licenseKey).useLicenseKey(true).build());

    MetricBuffer metricBuffer =
        new MetricBuffer(new Attributes().put("exampleName", "BoundaryExample"));
    metricBuffer.addMetric(getWithLongMetricName());
    metricBuffer.addMetric(getWithManyAttributes());
    metricBuffer.addMetric(getWithBigIntegerAttribute());
    metricBuffer.addMetric(getWithBigDecimalAttributes());
    metricBuffer.addMetric(getWithEmojiMetricName());
    metricBuffer.addMetric(getWithBasicMultilingualUnicodeMetricName());

    Response response = sender.sendBatch(metricBuffer.createBatch());
    logger.info(response.getBody());
  }

  /** At publication time, this did not cause errors in ingest. */
  private static Count getWithBasicMultilingualUnicodeMetricName() {
    Attributes attribs = new Attributes();
    attribs.put("finder", "getWithBasicMultilingualUnicodeMetricName");

    return new Count(
        "我是一个指标", 1.0, System.currentTimeMillis(), System.currentTimeMillis(), attribs);
  }

  /** At publication time, this did not cause errors in ingest. */
  private static Count getWithLongMetricName() {
    Attributes attribs = new Attributes();
    attribs.put("finder", "getWithLongMetricName");

    return new Count(
        "here is a very long metric name that is longer than the expected 30 characters",
        1.0,
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        attribs);
  }

  /** At publication time, this metric failed to be ingested and caused an NrIntegrationError. */
  private static Count getWithManyAttributes() {
    Attributes attribs = new Attributes();
    attribs.put("finder", "getWithManyAttributes");
    for (int i : IntStream.range(1, 50).toArray()) {
      attribs.put("attribute_" + String.valueOf(i), "myvalue");
    }

    return new Count(
        "attributeOverload", 1.0, System.currentTimeMillis(), System.currentTimeMillis(), attribs);
  }

  /** At publication time, this metric failed to be ingested and caused an NrIntegrationError */
  private static Count getWithBigIntegerAttribute() {
    Attributes attribs = new Attributes();
    attribs.put("finder", "getWithBigIntegerAttribute");
    attribs.put("bigInt", new BigInteger("2397349875872498384092830482"));
    attribs.put("asDouble", 2.3973498758724984e+27);
    return new Count(
        "bigIntegerAttribute",
        1.0,
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        attribs);
  }

  /**
   * At publication time, this did cause an NrIntegration. However, the metric did make it into the
   * system, but the superBig attribute got dropped.
   */
  private static Count getWithBigDecimalAttributes() {
    Attributes attribs = new Attributes();
    attribs.put("finder", "getWithBigDecimalAttributes");
    attribs.put(
        "superSmall", new BigDecimal(new BigInteger("2397349875872498384092830482"), 40000));
    attribs.put("superBig", new BigDecimal(new BigInteger("2397349875872498384092830482"), -40000));
    return new Count(
        "bigDecimalAttributes",
        1.0,
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        attribs);
  }

  /**
   * At publication time, this did not cause errors in ingest. YMMV with whether the emoji will be
   * viewable in the NR UI.
   */
  private static Count getWithEmojiMetricName() {
    Attributes attribs = new Attributes();
    attribs.put("finder", "getWithEmojiMetricName");

    return new Count(
        "emoji! \uD83C\uDDEF\uD83C\uDDF5 \uD83C\uDDF0\uD83C\uDDF7 \uD83C\uDDE9\uD83C\uDDEA \uD83C\uDDE8\uD83C\uDDF3 \uD83C\uDDFA\uD83C\uDDF8 \uD83C\uDDEB\uD83C\uDDF7 \uD83C\uDDEA\uD83C\uDDF8 \uD83C\uDDEE\uD83C\uDDF9 \uD83C\uDDF7\uD83C\uDDFA \uD83C\uDDEC\uD83C\uDDE7",
        1.0,
        System.currentTimeMillis(),
        System.currentTimeMillis(),
        attribs);
  }
}
