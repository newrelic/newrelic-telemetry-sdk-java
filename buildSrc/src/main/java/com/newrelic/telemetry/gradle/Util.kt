/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.kotlin.dsl.*
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar

fun Project.exampleClassTask(namespaceClassName: String) {
    val apiKey = properties.get("apiKey")?.toString() ?: ""
    val sourceSets = extensions["sourceSets"] as? SourceSetContainer ?: return

    task<JavaExec>(namespaceClassName.split(".").last()) {
        group = "examples"
        description = "Invokes $namespaceClassName#main"
        args = listOf(apiKey)
        classpath = sourceSets.getByName("main").runtimeClasspath
        main = namespaceClassName
    }.onlyIf { apiKey.isNotEmpty() }
}

//fun Project.standardPublishBoilerplate() {
//    val jar: Jar by tasks
//    jar.apply {
//        manifest.attributes["Implementation-Version"] = project.version
//        manifest.attributes["Implementation-Vendor"] = "New Relic, Inc"
//    }
//
//    val sonatype by configurations.creating {
//        extendsFrom(configurations["archives"])
//    }
//
//    val uploadSonatype by tasks.registering(org.gradle.api.tasks.Upload::class) {
//        configuration = configurations["sonatype"]
//
//        isUploadDescriptor = true
//    }
//
//    configure<NexusPluginExtension> {
//        if (project.properties["useLocalSonatype"] == "true") {
//            sign = false
//            snapshotRepositoryUrl = "http://admin:admin123@localhost:8081/repository/maven-snapshots/"
//            repositoryUrl = "http://admin:admin123@localhost:8081/repository/maven-releases/"
//        }
//        setConfiguration(sonatype)
//    }
//
//    val build by tasks.named("build")
//    build.dependsOn("javadoc")
//}