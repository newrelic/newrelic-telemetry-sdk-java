private object Versions {
    const val junit = "5.3.1"
    const val guava = "27.1-jre"
    const val mockito = "2.23.0"
    const val slf4j = "1.7.26"
    const val jsonassert = "1.5.0"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    "api"("org.slf4j:slf4j-api:${Versions.slf4j}")

    testImplementation("org.slf4j:slf4j-simple:${Versions.slf4j}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testImplementation("com.google.guava:guava:${Versions.guava}")
    testImplementation("org.skyscreamer:jsonassert:${Versions.jsonassert}")

}