import org.gradle.api.tasks.testing.logging.TestExceptionFormat

private object Versions {
    const val lombok = "1.18.6"
    const val junit = "5.3.1"
    const val mockito = "2.23.0"
    const val mockserver = "5.5.1"
    const val slf4j = "1.7.26"
    const val okhttp = "3.14.1"
    const val guava = "27.1-jre"
    const val testContainer = "1.11.3"
}
repositories {
    mavenCentral()
    jcenter()
}

plugins {
    java
}

dependencies {
    implementation("org.mock-server:mockserver-client-java:${Versions.mockserver}")
    implementation(project(":metrics"))
    implementation(project(":telemetry_components"))

    testCompileOnly("org.projectlombok:lombok:${Versions.lombok}")
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.lombok}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${Versions.junit}")
    testImplementation("com.squareup.okhttp3:okhttp:${Versions.okhttp}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.slf4j:slf4j-simple:${Versions.slf4j}")
    testImplementation("com.google.guava:guava:${Versions.guava}")
    testImplementation("org.testcontainers:testcontainers:${Versions.testContainer}")
    testImplementation("org.testcontainers:junit-jupiter:${Versions.testContainer}")
}

tasks {
    test {
        useJUnitPlatform()

//        testLogging.showStandardStreams = true
        testLogging.exceptionFormat = TestExceptionFormat.FULL
        testClassesDirs = sourceSets["test"].output.classesDirs
        classpath = sourceSets["test"].runtimeClasspath
    }
}
