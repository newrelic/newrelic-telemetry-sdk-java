package com.newrelic.telemetry.gradle

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.signing.SigningExtension

fun PublishingExtension.configureRepositories(project: Project, useLocalSonatype: Boolean, publicationName: String) {
    val publishing = this;
    repositories {
        maven {
            if (useLocalSonatype) {
                val releasesRepoUrl = project.uri("http://localhost:8081/repository/maven-releases/")
                val snapshotsRepoUrl = project.uri("http://localhost:8081/repository/maven-snapshots/")
                url = if (project.version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            }
            else {
                val releasesRepoUrl = project.uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = project.uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if (project.version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                project.configure<SigningExtension> {
                    this.sign(publishing.publications[publicationName])
                }
            }
            credentials {
                username = System.getenv("SONATYPE_USERNAME")

                if ((username?.length ?: 0) == 0){
                    username = project.properties["sonatypeUsername"] as String?
                }
                password = System.getenv("SONATYPE_PASSWORD")
                if ((password?.length ?: 0) == 0) {
                    password = project.properties["sonatypePassword"] as String?
                }
            }
        }
    }
}