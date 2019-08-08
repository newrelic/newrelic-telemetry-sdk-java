import com.newrelic.telemetry.gradle.standardPublishBoilerplate

private object Versions {
    const val junit = "5.3.1"
    const val mockito = "2.23.0"
    const val slf4j = "1.7.26"
}

plugins {
    java
}

apply(plugin = "java-library")
apply(plugin = "com.bmuschko.nexus")

dependencies {
    "api"(project(":metrics"))

    testImplementation("org.slf4j:slf4j-simple:${Versions.slf4j}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
}

standardPublishBoilerplate()
