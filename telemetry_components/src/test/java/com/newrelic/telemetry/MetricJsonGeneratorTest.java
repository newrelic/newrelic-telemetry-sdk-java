/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.newrelic.telemetry.model.Attributes;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class MetricJsonGeneratorTest {
  @Test
  @DisplayName("Formatting with common attributes is structured correctly")
  void testCommonAttributeJson() throws Exception {
    MetricBatchJsonGenerator jsonGenerator =
        new MetricBatchJsonGenerator(new MetricGsonGenerator());

    Attributes commonAttributes = new Attributes().put("key", "val");

    MetricBatch metricBatch =
        new MetricBatch(
            Collections.singletonList(new Gauge("gauge", 3d, 555, new Attributes())),
            commonAttributes);
    String json = jsonGenerator.generateJson(metricBatch);

    String expected =
        "[{\"common\":{\"attributes\":{\"key\":\"val\"}},\"metrics\":"
            + "[{\"name\":\"gauge\",\"type\":\"gauge\",\"value\":3.0,\"timestamp\":555}]}]";
    JSONAssert.assertEquals(expected, json, false);
  }

  @Test
  @DisplayName("All supported attribute types are structured correctly")
  void testAttributeTypesJson() throws Exception {
    MetricBatchJsonGenerator jsonGenerator =
        new MetricBatchJsonGenerator(new MetricGsonGenerator());

    Attributes commonAttributes = new Attributes();
    commonAttributes.put("string", "val");
    commonAttributes.put("double", 4.4d);
    commonAttributes.put("float", 4.32f);
    commonAttributes.put("int", 5);
    commonAttributes.put("long", 384949494949499999L);
    commonAttributes.put("boolean", true);
    commonAttributes.put("number", new BigDecimal("55.555"));
    commonAttributes.put("null", (Number) null);

    Attributes metricAttributes =
        new Attributes()
            .put("string", "other")
            .put("double", 1.1d)
            .put("float", 7.554f)
            .put("int", 99)
            .put("long", 980980980980808098L)
            .put("boolean", false)
            .put("number", new BigDecimal("-0.555"));

    MetricBatch metricBatch =
        new MetricBatch(
            Collections.singletonList(new Count("count", 3, 555, 666, metricAttributes)),
            commonAttributes);
    String json = jsonGenerator.generateJson(metricBatch);

    String expected =
        "[{\"common\":{\"attributes\":{\"string\":\"val\",\"double\":4.4,\"float\":4.32,\"int\":5,\"long\":384949494949499999,\"boolean\":true,\"number\":55.555}}"
            + ",\"metrics\":[{\"name\":\"count\",\"type\":\"count\",\"value\":3.0,\"timestamp\":555,\"interval.ms\":111,"
            + "\"attributes\":{\"string\":\"other\",\"double\":1.1,\"float\":7.554,\"int\":99,\"long\":980980980980808098,\"boolean\":false,\"number\":-0.555}}]}]";
    JSONAssert.assertEquals(expected, json, false);
  }

  @Test
  void testSummaryMetricJson() throws Exception {
    MetricBatchJsonGenerator jsonGenerator =
        new MetricBatchJsonGenerator(new MetricGsonGenerator());

    Attributes commonAttributes = new Attributes().put("key", "val");

    MetricBatch metricBatch =
        new MetricBatch(
            Collections.singletonList(
                new Summary("summary", 3, 33d, 55d, 66d, 555, 666, new Attributes())),
            commonAttributes);
    String json = jsonGenerator.generateJson(metricBatch);

    String expected =
        "[{\"common\":{\"attributes\":{\"key\":\"val\"}},\"metrics\":[{\"name\":\"summary\",\"type\":\"summary\",\"value\":{\"count\":3,\"sum\":33.0,\"min\":55.0,\"max\":66.0},\"timestamp\":555,\"interval.ms\":111,\"attributes\":{}}]}]";
    JSONAssert.assertEquals(expected, json, false);
  }

  @Test
  @DisplayName("Common attribute formatting handles values that cause the JSON serializer to fail")
  void testAttributeValuesThatCannotBeRepresented() throws Exception {
    MetricBatchJsonGenerator jsonGenerator =
        new MetricBatchJsonGenerator(new MetricGsonGenerator());

    Attributes commonAttributes = new Attributes();
    // filtered items
    commonAttributes.put("key-nan", Double.NaN);
    commonAttributes.put("key-neg-inf", Double.NEGATIVE_INFINITY);
    commonAttributes.put("key-pos-inf", Double.POSITIVE_INFINITY);
    commonAttributes.put(
        "key-bigdec-pos-inf",
        new BigDecimal(
            new BigInteger("12312312312312312312312312312312312312312312312312"), -34567));

    MetricBatch metricBatch =
        new MetricBatch(
            Collections.singletonList(new Count("count", 3, 555, 666, new Attributes())),
            commonAttributes);
    String json = jsonGenerator.generateJson(metricBatch);

    String expected =
        "[{\"common\":{\"attributes\":{}},\"metrics\":[{\"name\":\"count\",\"type\":\"count\",\"value\":3.0,\"timestamp\":555,\"interval.ms\":111,\"attributes\":{}}]}]";
    JSONAssert.assertEquals(expected, json, false);
  }

  @Test
  @DisplayName("Metric formatting handles values that cause the JSON serializer to fail")
  void testMetricValuesThatCannotBeRepresented() throws Exception {
    MetricBatchJsonGenerator jsonGenerator =
        new MetricBatchJsonGenerator(new MetricGsonGenerator());

    Attributes commonAttributes = new Attributes();

    MetricBatch metricBatch =
        new MetricBatch(
            Arrays.asList(
                new Count("countNaN", Double.NaN, 555, 666, new Attributes()),
                new Count("countNegInf", Double.NEGATIVE_INFINITY, 555, 666, new Attributes()),
                new Count("countPosInf", Double.POSITIVE_INFINITY, 555, 666, new Attributes()),
                new Gauge("gaugeNaN", Double.NaN, 555, new Attributes()),
                new Gauge("gaugeNegInf", Double.NEGATIVE_INFINITY, 555, new Attributes()),
                new Gauge("gaugePosInf", Double.POSITIVE_INFINITY, 555, new Attributes()),
                new Summary(
                    "summarySumBad", 100, Double.NaN, 1000d, 1000d, 555, 666, new Attributes()),
                new Summary(
                    "summaryMinBad", 100, 1000d, Double.NaN, 1000d, 555, 666, new Attributes()),
                new Summary(
                    "summaryMaxBad", 100, 1000d, 1000d, Double.NaN, 555, 666, new Attributes())),
            commonAttributes);
    String json = jsonGenerator.generateJson(metricBatch);

    String expected = "[{\"metrics\":[]}]";
    JSONAssert.assertEquals(expected, json, false);
  }

  @Test
  @DisplayName(
      "Common attribute formatting handles values that probably cannot be ingested successfully")
  void testStrangeAndUnusualValues() throws Exception {
    MetricBatchJsonGenerator jsonGenerator =
        new MetricBatchJsonGenerator(new MetricGsonGenerator());

    Attributes commonAttributes = new Attributes();
    // filtered items
    // included items
    commonAttributes.put(
        "key-bigdec-very-small",
        new BigDecimal(
            new BigInteger("12312312312312312312312312312312312312312312312312"), 34567));
    commonAttributes.put(
        "key-bigint-weird", new BigInteger("12312312312312312312312312312312312312312312312312"));
    commonAttributes.put("unicode-key-\uD83D\uDD25", "\uD83D\uDCA5");

    MetricBatch metricBatch =
        new MetricBatch(
            Collections.singletonList(new Count("count", 3, 555, 666, new Attributes())),
            commonAttributes);
    String json = jsonGenerator.generateJson(metricBatch);

    assertTrue(
        json.contains("\"key-bigint-weird\":12312312312312312312312312312312312312312312312312"));
    assertTrue(json.contains("\"unicode-key-\uD83D\uDD25\":\"\uD83D\uDCA5\""));
    assertTrue(
        json.contains(
            "\"key-bigdec-very-small\":1.2312312312312312312312312312312312312312312312312E-34518"));
  }

  @Test
  @DisplayName("Attributes is generated correctly with gson [move this test to the gson tester]")
  void testAttributes() throws Exception {
    MetricGsonGenerator metricGsonGenerator = new MetricGsonGenerator();
    assertEquals("{}", metricGsonGenerator.writeAttributes(Collections.emptyMap()));
    JSONAssert.assertEquals(
        "{\"foo\":\"bar\"}",
        metricGsonGenerator.writeAttributes(Collections.singletonMap("foo", "bar")),
        false);
  }
}
