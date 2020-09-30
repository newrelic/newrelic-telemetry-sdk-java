package com.newrelic.telemetry.gradle

import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.signing.SigningExtension

fun PublishingExtension.configureRepositories(project: Project, useLocalSonatype: Boolean, publicationName: String, release: String?) {
    val publishing = this;
    repositories {
        maven {
            if (useLocalSonatype) {
                val releasesRepoUrl = project.uri("http://localhost:8081/repository/maven-releases/")
                val snapshotsRepoUrl = project.uri("http://localhost:8081/repository/maven-snapshots/")
                url = if ("true" == release) releasesRepoUrl else snapshotsRepoUrl
            }
            else {
                val releasesRepoUrl = project.uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
                val snapshotsRepoUrl = project.uri("https://oss.sonatype.org/content/repositories/snapshots/")
                url = if("true" == release) releasesRepoUrl else snapshotsRepoUrl
                project.configure<SigningExtension> {
                    val signingKey : String? = project.properties["signingKey"] as String?
                    val signingKeyId: String? = project.properties["signingKeyId"] as String?
                    val signingPassword: String? = project.properties["signingPassword"] as String?
                    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
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
