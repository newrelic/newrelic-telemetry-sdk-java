import com.newrelic.telemetry.gradle.configureRepositories
import com.newrelic.telemetry.gradle.configuredPom

plugins {
    java
    id("java-library")
    id("maven-publish")
    id("signing")
    id("com.github.sherter.google-java-format") version "0.8"
}

allprojects {
    group = "com.newrelic.telemetry"
    val release: String? by project
    version = if("true" == release) version else "${version}-SNAPSHOT"
    repositories {
        mavenCentral()
        jcenter()
    }
    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
}

listOf(":telemetry", ":telemetry-http-okhttp", ":telemetry-http-java11", ":telemetry-all").forEach {
    project(it) {
        apply(plugin = "java-library")
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
        val release: String? by project
        version = if("true" == release) version else "${version}-SNAPSHOT"
        tasks {
            val taskScope = this
            val sources = sourceSets
            val sourcesJar by creating(Jar::class) {
                dependsOn(JavaPlugin.CLASSES_TASK_NAME)
                archiveClassifier.set("sources")
                from(sources.main.get().allSource)
            }

            val javadocJar by creating(Jar::class) {
                dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
                archiveClassifier.set("javadoc")
                from(taskScope.javadoc)
            }

            val jar: Jar by taskScope
            jar.apply {
                manifest {
                    attributes(mapOf(
                            "Implementation-Version" to project.version,
                            "Implementation-Vendor" to "New Relic, Inc.",
                            "Automatic-Module-Name" to "com.newrelic.telemetry" // Hack
                    ))
                }
            }
        }
        val useLocalSonatype = project.properties["useLocalSonatype"] == "true"
        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])
                    artifact(tasks["sourcesJar"])
                    artifact(tasks["javadocJar"])
                    configuredPom(project)
                }
            }
            configureRepositories(project, useLocalSonatype, "mavenJava", release)
        }
    }
}