import com.newrelic.telemetry.gradle.standardPublishBoilerplate

private object Versions {
    const val gson = "2.8.5"
    const val okhttp = "3.14.1"
    const val junit = "5.3.1"
    const val jsonassert = "1.5.0"
}

plugins {
    java
}

apply(plugin = "java-library")
apply(plugin = "com.bmuschko.nexus")

dependencies {
    "api"(project(":metrics"))
    "api"("com.google.code.gson:gson:${Versions.gson}")
    "api"("com.squareup.okhttp3:okhttp:${Versions.okhttp}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
    testImplementation("org.skyscreamer:jsonassert:${Versions.jsonassert}")
}

standardPublishBoilerplate()
