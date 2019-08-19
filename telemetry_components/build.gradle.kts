
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

    // TODO prefer the lazy string invoke once https://github.com/gradle/gradle-native/issues/718 is fixed
    getByName<Upload>("uploadArchives") {

        repositories {

            withConvention(MavenRepositoryHandlerConvention::class) {

                mavenDeployer {

                    withGroovyBuilder {
                        "repository"("url" to uri("$buildDir/repo"))
                        "snapshotRepository"("url" to uri("$buildDir/repo"))
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

publishing {
    repositories {
        maven {
            // change to point to your repo, e.g. http://my.org/repo
            url = uri("$buildDir/repo")
            mavenLocal {

            }
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(tasks["sourcesJar"])
        }
    }
}

