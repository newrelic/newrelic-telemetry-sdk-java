package com.newrelic.telemetry.gradle

import org.gradle.api.Project

fun org.gradle.api.publish.maven.MavenPublication.configuredPom(project: Project) {
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