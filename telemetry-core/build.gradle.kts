import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.newrelic.telemetry.gradle.configureRepositories
import com.newrelic.telemetry.gradle.configuredPom

plugins {
    java
    id("java-library")
    id("com.github.johnrengelman.shadow")
}

val junitVersion: String by project
val jsonassertVersion: String by project
val mockitoVersion: String by project
val guavaVersion: String by project
val slf4jVersion: String by project
val gsonVersion: String by project

apply(plugin = "maven-publish")
apply(plugin = "signing")

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

configurations["shadow"].extendsFrom(configurations["api"])

dependencies {
    api("org.slf4j:slf4j-api:${slf4jVersion}")
    implementation("com.google.code.gson:gson:${gsonVersion}")

    testImplementation("org.slf4j:slf4j-simple:${slf4jVersion}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${junitVersion}")
    testImplementation("org.mockito:mockito-core:${mockitoVersion}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${junitVersion}")
    testImplementation("com.google.guava:guava:${guavaVersion}")
    testImplementation("org.skyscreamer:jsonassert:${jsonassertVersion}")
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
            exclude(dependency("org.slf4j:slf4j-api:${slf4jVersion}"))
        }
        manifest {
            attributes(mapOf("Implementation-Version" to project.version, "Implementation-Vendor" to "New Relic, Inc."))
        }
        exclude("**/module-info.class")
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