import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.newrelic.telemetry.gradle.configureRepositories
import com.newrelic.telemetry.gradle.configuredPom

plugins {
    java
    id("java-library")
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

apply(plugin = "maven-publish")
apply(plugin = "signing")

private object Versions {
    const val junit = "5.3.1"
    const val guava = "27.1-jre"
    const val mockito = "2.23.0"
    const val slf4j = "1.7.30"
    const val jsonassert = "1.5.0"
    const val gson = "2.8.6"
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

configurations["shadow"].extendsFrom(configurations["api"])

dependencies {
    api("org.slf4j:slf4j-api:${Versions.slf4j}")
    implementation("com.google.code.gson:gson:${Versions.gson}")

    testImplementation("org.slf4j:slf4j-simple:${Versions.slf4j}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testImplementation("com.google.guava:guava:${Versions.guava}")
    testImplementation("org.skyscreamer:jsonassert:${Versions.jsonassert}")
}

val javadocJar by tasks.creating(Jar::class) {
    from(tasks["javadoc"])
    archiveClassifier.set("javadoc")
}

val sourcesJar by tasks.creating(Jar::class) {
    dependsOn(JavaPlugin.CLASSES_TASK_NAME)
    from(sourceSets["main"].allJava)
    archiveClassifier.set("sources")
}


tasks {
    "shadowJar"(ShadowJar::class) {
        archiveClassifier.set("")
        dependencies {
            exclude(dependency("org.slf4j:slf4j-api:${Versions.slf4j}"))
        }
        manifest {
            attributes(mapOf("Implementation-Version" to project.version, "Implementation-Vendor" to "New Relic, Inc."))
        }
        relocate("com.google.gson", "com.newrelic.relocated")
        minimize()
    }
    val propertiesDir = "build/generated/properties"
    val versionFilename = "telemetry.sdk.version.properties"
    sourceSets.get("main").output.dir(mapOf("builtBy" to "generateVersionResource"), propertiesDir)
    register("generateVersionResource") {
        outputs.file(File("$propertiesDir/$versionFilename"))
        doLast {
            val folder = file(propertiesDir)
            folder.mkdirs()
            val propertiesFile = File(folder.getAbsolutePath(), versionFilename)
            propertiesFile.writeText("${project.version}")
        }
    }

    build {
        dependsOn(shadowJar)
        dependsOn("generateVersionResource")
    }

}

val useLocalSonatype = project.properties["useLocalSonatype"] == "true"
val release: String? by project

configure<PublishingExtension> {
    publications {
        create<MavenPublication>("mavenJava") {
            project.shadow.component(this)
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            configuredPom(project)
        }
    }
    configureRepositories(project, useLocalSonatype, "mavenJava", release)
}