/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.boundaries;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.Count;
import com.newrelic.telemetry.MetricBatchSender;
import com.newrelic.telemetry.MetricBuffer;
import com.newrelic.telemetry.Response;
import com.newrelic.telemetry.SimpleMetricBatchSender;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.logging.Logger;
import java.util.stream.IntStream;

public class BoundaryExample {
  private static final Logger logger = Logger.getLogger(BoundaryExample.class.getName());

  public static void main(String[] args) throws MalformedURLException {
    MetricBatchSender sender =
        SimpleMetricBatchSender.builder(args[0])
            .uriOverride(URI.create("https://staging-metric-api.newrelic.com"))
            .build();

    MetricBuffer metricBuffer = new MetricBuffer(new Attributes());
    metricBuffer.addMetric(getWithLongMetricName());
    metricBuffer.addMetric(getWithManyAttributes());
    metricBuffer.addMetric(getWithBigIntegerAttribute());
    metricBuffer.addMetric(getWithBigDecimalAttributes());
    metricBuffer.addMetric(getWithEmojiMetricName());
    metricBuffer.addMetric(getWithBasicMultilingualUnicodeMetricName());

    try {
      Response response = sender.sendBatch(metricBuffer.createBatch());
      logger.info(response.getBody());
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private static Count getWithBasicMultilingualUnicodeMetricName() {
    Attributes attribs = new Attributes();
    attribs.put("finder", "getWithBasicMultilingualUnicodeMetricName");

    return new Count(
        "我是一个指标", 1.0, System.currentTimeMillis(), System.currentTimeMillis(), attribs);
  }

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

  private static Count getWithManyAttributes() {
    Attributes attribs = new Attributes();
    attribs.put("finder", "getWithManyAttributes");
    for (int i : IntStream.range(1, 50).toArray()) {
      attribs.put("attribute_" + String.valueOf(i), "myvalue");
    }

    return new Count(
        "attributeOverload", 1.0, System.currentTimeMillis(), System.currentTimeMillis(), attribs);
  }

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
