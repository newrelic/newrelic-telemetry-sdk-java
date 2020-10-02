import org.gradle.api.tasks.testing.logging.TestExceptionFormat

val guavaVersion: String by project
val junitVersion: String by project
val mockitoVersion: String by project
val mockserverVersion: String by project
val okhttpVersion: String by project
val slf4jVersion: String by project
val gsonVersion: String by project
val testContainerVersion: String by project

repositories {
    mavenCentral()
    jcenter()
}

plugins {
    java
}

dependencies {
    implementation("org.mock-server:mockserver-client-java:${mockserverVersion}")
    implementation(project(":telemetry-core"))
    implementation(project(":telemetry-http-okhttp"))

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-params:${junitVersion}")
    testImplementation("com.squareup.okhttp3:okhttp:${okhttpVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation("org.slf4j:slf4j-simple:${slf4jVersion}")
    testImplementation("com.google.guava:guava:${guavaVersion}")
    testImplementation("org.testcontainers:testcontainers:${testContainerVersion}")
    testImplementation("org.testcontainers:junit-jupiter:${testContainerVersion}")
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
