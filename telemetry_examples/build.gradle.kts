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
    runtimeOnly("org.slf4j:slf4j-simple:${Versions.slf4j}")
}

exampleClassTask("com.newrelic.telemetry.count.CountExample")
exampleClassTask("com.newrelic.telemetry.gauge.GaugeExample")
exampleClassTask("com.newrelic.telemetry.summary.SummaryExample")
exampleClassTask("com.newrelic.telemetry.boundaries.BoundaryExample")