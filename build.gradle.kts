import com.newrelic.telemetry.gradle.configureRepositories
import com.newrelic.telemetry.gradle.configuredPom

plugins {
    java
}

apply(plugin = "java")
apply(plugin = "java-library")
apply(plugin = "maven-publish")
apply(plugin = "signing")

apply(plugin = "com.github.sherter.google-java-format")

allprojects {
    group = "com.newrelic.telemetry"
    version = project.findProperty("releaseVersion") as String
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

listOf(":telemetry", ":telemetry-http-okhttp", ":telemetry-http-java11").forEach {
    project(it) {
        apply(plugin = "java-library")
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
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
                manifest.attributes["Implementation-Version"] = project.version
                manifest.attributes["Implementation-Vendor"] = "New Relic, Inc"
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
            configureRepositories(project, useLocalSonatype, "mavenJava")
        }
    }
}
