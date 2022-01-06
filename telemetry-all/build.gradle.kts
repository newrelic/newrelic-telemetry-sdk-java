plugins {
    java
    id("java-library")
    id( "org.ysb33r.java.modulehelper")
}

val slf4jVersion: String by project
val gsonVersion: String by project

configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

sourceSets {
    main {
        java.setSrcDirs(listOf(
                "src/main/java",
                ".generated-src/java"
        ))
        resources.setSrcDirs(listOf(
                "src/main/resources",
                ".generated-src/resources"
        ))
    }
}

dependencies {
    api("org.slf4j:slf4j-api:${slf4jVersion}")
    implementation("com.google.code.gson:gson:${gsonVersion}")
}

extraJavaModules {
    module("slf4j-api-${slf4jVersion}.jar", "org.slf4j", slf4jVersion) {
        exports("org.slf4j")
    }
    module("gson-${gsonVersion}.jar", "com.google.code.gson", gsonVersion) {
        exports("com.google.gson")
    }
}

tasks.register<Copy>("copySources") {
    group = "Build"
    description = "Copies sources from other subprojects"
    into(".generated-src")
    from ("../telemetry-core/src/main/java") {
        into ("java")
    }
    from ("../telemetry-http-java11/src/main/java") {
        into ("java")
    }
    from ("../telemetry-core/src/main/resources") {
        into ("resources")
    }
    from ("../telemetry-http-java11/src/main/resources") {
        into ("resources")
    }
}

tasks.named("compileJava") {
    dependsOn("copySources")
}

tasks.named("processResources") {
    dependsOn("copySources")
}

tasks.named<Delete>("clean") {
    delete (".generated-src")
}