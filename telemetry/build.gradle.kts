val junitVersion: String by project
val mockitoVersion: String by project
val slf4jVersion: String by project

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    api(project(":telemetry-core"))

    testImplementation("org.slf4j:slf4j-simple:${slf4jVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
}