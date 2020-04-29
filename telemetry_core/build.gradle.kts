import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("com.github.johnrengelman.shadow") version "5.1.0"
}

apply(plugin = "com.github.johnrengelman.shadow")

private object Versions {
    const val junit = "5.3.1"
    const val guava = "27.1-jre"
    const val mockito = "2.23.0"
    const val slf4j = "1.7.26"
    const val jsonassert = "1.5.0"
    const val gson = "2.8.6"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    "api"("org.slf4j:slf4j-api:${Versions.slf4j}")
    compile("com.google.code.gson:gson:${Versions.gson}")

    testImplementation("org.slf4j:slf4j-simple:${Versions.slf4j}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testImplementation("com.google.guava:guava:${Versions.guava}")
    testImplementation("org.skyscreamer:jsonassert:${Versions.jsonassert}")

}

tasks {
    "shadowJar"(ShadowJar::class) {
        classifier = ""
        dependencies {
            exclude(dependency("org.slf4j:slf4j-api:${Versions.slf4j}"))
        }
        relocate("com.google.gson", "com.newrelic.relocated")
        minimize()
    }
    build {
        dependsOn(shadowJar)
    }
}
