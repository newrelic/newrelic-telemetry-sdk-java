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
                "../telemetry-core/src/main/java",
                "../telemetry/src/main/java",
                "../telemetry-http-java11/src/main/java"
        ))
        resources.setSrcDirs(listOf(
                "../telemetry-core/src/main/resources",
                "../telemetry/src/main/resources",
                "../telemetry-http-java11/src/main/resources"
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
