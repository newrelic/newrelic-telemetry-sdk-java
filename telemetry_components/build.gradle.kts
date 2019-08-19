private object Versions {
    const val gson = "2.8.5"
    const val okhttp = "3.14.1"
    const val junit = "5.3.1"
    const val jsonassert = "1.5.0"
}

plugins {
    java
    `maven-publish`
    maven
}

repositories {
    jcenter()
}

apply(plugin = "java")
apply(plugin = "java-library")
//apply(plugin = "maven-publish")

dependencies {
    "api"(project(":metrics"))
    "api"("com.google.code.gson:gson:${Versions.gson}")
    "api"("com.squareup.okhttp3:okhttp:${Versions.okhttp}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
    testImplementation("org.skyscreamer:jsonassert:${Versions.jsonassert}")
}

tasks {
    val sourcesJar by creating(Jar::class) {
        dependsOn(JavaPlugin.CLASSES_TASK_NAME)
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    val javadocJar by creating(Jar::class) {
        dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
        archiveClassifier.set("javadoc")
        from(javadoc)
    }

    artifacts {
        add("archives", sourcesJar)
        add("archives", javadocJar)
    }

    getByName<Upload>("uploadArchives") {

        repositories {
            withConvention(MavenRepositoryHandlerConvention::class) {
                mavenDeployer {
                    withGroovyBuilder {
                        if (project.properties["useLocalSonatype"] == "true") {
                            val localCredentials = mapOf(
                                "userName" to project.properties["localSonatypeUser"],
                                "password" to project.properties["localSonatypePassword"]
                            )
                            "repository"("url" to "http://localhost:8081/repository/maven-releases/") {
                                "authentication"(localCredentials)
                            }
                            "snapshotRepository"("url" to "http://localhost:8081/repository/maven-snapshots/") {
                                "authentication"(localCredentials)
                            }
                        } else {
                            "repository"("url" to "https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                            "snapshotRepository"("url" to "https://oss.sonatype.org/content/repositories/snapshots/")
                        }
                    }

                    pom.project {
                        withGroovyBuilder {
                            "licenses" {
                                "license" {
                                    "name"("The Apache Software License, Version 2.0")
                                    "url"("http://www.apache.org/licenses/LICENSE-2.0.txt")
                                    "distribution"("repo")
                                }
                            }
                            "description"("This module contains reference implementations of the required interfaces for the SDK to function.")
                            "name"(project.name)
                            "url"("https://github.com/newrelic/newrelic-telemetry-sdk-java")
                            "scm" {
                                "url"("git@github.com:newrelic/newrelic-telemetry-sdk-java.git")
                                "connection"("scm:git:git@github.com:newrelic/newrelic-telemetry-sdk-java.git")
                            }
                            "developers" {
                                "developer" {
                                    "id"("newrelic")
                                    "name"("New Relic")
                                    "email"("opensource@newrelic.com")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
