/*
 * ---------------------------------------------------------------------------------------------
 *  Copyright (c) 2019 New Relic Corporation. All rights reserved.
 *  Licensed under the Apache 2.0 License. See LICENSE in the project root directory for license information.
 * --------------------------------------------------------------------------------------------
 */

package com.newrelic.telemetry.gradle

import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.task

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
