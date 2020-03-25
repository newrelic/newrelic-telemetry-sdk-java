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

listOf(":telemetry-core", ":telemetry", ":telemetry-http-okhttp", ":telemetry-http-java11").forEach {
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
                    pom {
                        name.set(project.name)
                        description.set("Used to send telemetry data to New Relic")
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
                    }
                    else {
                        val releasesRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                        val snapshotsRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
                        url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                        configure<SigningExtension> {
                            sign(publications["mavenJava"])
                        }
                    }
                    credentials {
                        username = project.properties["sonatypeUsername"] as String?
                        password = project.properties["sonatypePassword"] as String?
                    }
                }
            }
        }
    }
}
