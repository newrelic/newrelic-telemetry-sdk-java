/*
 * Copyright 2020 New Relic Corporation. All rights reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

import com.newrelic.telemetry.gradle.exampleClassTask

private object Versions {
    const val slf4j = "1.7.26"
}

plugins {
    java
}

apply(plugin = "java-library")

dependencies {
    implementation(project(":telemetry"))
    implementation(project(":telemetry-http-okhttp"))
    implementation(project(":telemetry-http-java11"))
    runtimeOnly("org.slf4j:slf4j-simple:${Versions.slf4j}")
}

tasks {
    javadoc {
        options.encoding = "UTF-8"
    }
}

exampleClassTask("com.newrelic.telemetry.examples.BoundaryExample")
exampleClassTask("com.newrelic.telemetry.examples.ConfigurationExamples")
exampleClassTask("com.newrelic.telemetry.examples.CountExample")
exampleClassTask("com.newrelic.telemetry.examples.EventExample")
exampleClassTask("com.newrelic.telemetry.examples.GaugeExample")
exampleClassTask("com.newrelic.telemetry.examples.LogExample")
exampleClassTask("com.newrelic.telemetry.examples.SpanExample")
exampleClassTask("com.newrelic.telemetry.examples.SummaryExample")
exampleClassTask("com.newrelic.telemetry.examples.TelemetryClientExample")
exampleClassTask("com.newrelic.telemetry.examples.SpanToTraceObserverExample")
