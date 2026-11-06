val junitVersion: String by project
val junitPlatformVersion: String by project
val jsonassertVersion: String by project

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    api(project(":telemetry-core"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:${junitPlatformVersion}")
    testImplementation("org.skyscreamer:jsonassert:${jsonassertVersion}")
}
