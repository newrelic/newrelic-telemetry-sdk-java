buildscript {
    dependencies {
        classpath("gradle.plugin.com.github.sherter.google-java-format:google-java-format-gradle-plugin:0.8")
    }
}

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

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

listOf(":metrics", ":telemetry", ":telemetry-components").forEach {
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
        }
        val useLocalSonatype = project.properties["useLocalSonatype"] == "true"

        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("mavenJava") {
                    from(components["java"])
                    artifact(tasks["sourcesJar"])
                    artifact(tasks["javadocJar"])
                    pom {
                        name.set(project.name)
                        description.set("This module contains reference implementations of the required interfaces for the SDK to function.")
                        url.set("https://github.com/newrelic/newrelic-telemetry-sdk-java")
                        licenses {
                            license {
                                name.set("The Apache License, Version 2.0")
                                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                                distribution.set("repo")
                            }
                        }
                        developers {
                            developer {
                                id.set("newrelic")
                                name.set("New Relic")
                                email.set("opensource@newrelic.com")
                            }
                        }
                        scm {
                            url.set("git@github.com:newrelic/newrelic-telemetry-sdk-java.git")
                            connection.set("scm:git:git@github.com:newrelic/newrelic-telemetry-sdk-java.git")
                        }
                    }
                }
            }
            repositories {
                maven {
                    if (useLocalSonatype) {
                        val releasesRepoUrl = uri("http://localhost:8081/repository/maven-releases/")
                        val snapshotsRepoUrl = uri("http://localhost:8081/repository/maven-snapshots/")
                        url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                        credentials {
                            username = project.properties["localSonatypeUser"] as String?
                            password = project.properties["localSonatypePassword"] as String?
                        }
                    }
                    else{
                        val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                        val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                        url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                        configure<SigningExtension> {
                            sign(publications["mavenJava"])
                        }
                    }
                }
            }
        }

    }
}