private object Versions {
    const val gson = "2.8.5"
    const val okhttp = "3.14.1"
    const val junit = "5.3.1"
    const val jsonassert = "1.5.0"
}

plugins {
    `java-library`
    `maven-publish`
    maven
    signing
}

repositories {
    jcenter()
}

dependencies {
    "api"(project(":metrics"))
    "api"("com.google.code.gson:gson:${Versions.gson}")
    "api"("com.squareup.okhttp3:okhttp:${Versions.okhttp}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
    testImplementation("org.skyscreamer:jsonassert:${Versions.jsonassert}")
}

tasks.register<Jar>("sourcesJar") {
    from(sourceSets.main.get().allJava)
    archiveClassifier.set("sources")
}

tasks.register<Jar>("javadocJar") {
    from(tasks.javadoc)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "telemetry-components"
            groupId = "com.newrelic.telemetry"
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
            if (project.properties["useLocalSonatype"] == "true") {
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
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

tasks.javadoc {
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
/*

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
//                    if (project.properties["useLocalSonatype"] != "true") {
                        beforeDeployment {
                            signing.signPom(this)
                            signing.sign(publishing.publications.get())
                        }
//                        beforeDeployment {
//                            signing.sign(configurations.archives.get())
//                        }
//                    }

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
}*/
