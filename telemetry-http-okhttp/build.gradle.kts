val junitVersion: String by project
val jsonassertVersion: String by project
val okhttpVersion: String by project

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(project(":telemetry"))
    api(project(":telemetry-core"))
    api("com.squareup.okhttp3:okhttp:${okhttpVersion}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testImplementation("org.skyscreamer:jsonassert:${jsonassertVersion}")
}
