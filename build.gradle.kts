buildscript {
    dependencies {
        classpath("gradle.plugin.com.github.sherter.google-java-format:google-java-format-gradle-plugin:0.8")
        classpath ("com.bmuschko:gradle-nexus-plugin:2.3.1")
    }
}

plugins {
    java
}

apply(plugin = "java-library")
apply(plugin = "com.github.sherter.google-java-format")
apply(plugin = "com.bmuschko.nexus")

allprojects {
    group = "com.newrelic.telemetry"
    version = project.findProperty("releaseVersion") as String
    repositories {
        mavenCentral()
        jcenter()
    }
    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

