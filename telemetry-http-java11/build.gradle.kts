val junitVersion: String by project
val jsonassertVersion: String by project

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    implementation(project(":telemetry-client"))
    api(project(":telemetry-core"))

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testImplementation("org.skyscreamer:jsonassert:${jsonassertVersion}")
}
