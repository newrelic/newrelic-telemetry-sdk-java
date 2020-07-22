private object Versions {
//    const val junit = "5.3.1"
//    const val jsonassert = "1.5.0"
}

plugins {
    java
    id("java-library")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    "implementation"(project(":telemetry-core"))
    "implementation"(project(":telemetry"))
    "implementation"(project(":telemetry-http-java11"))
}

plugins.withType<JavaPlugin>().configureEach {
    configure<JavaPluginExtension> {
        modularity.inferModulePath.set(true)
    }
}

tasks {
    val taskScope = this


    // closureOf<Dependency>(
    val isDependency = closureOf<>{d : Dependency -> d is ProjectDependency} // : (Dependency) -> Boolean

    val collector = { d : Dependency -> d.dependencyProject.sourceSets.main.output }

    val jar: Jar by taskScope
    jar.apply {
        archiveClassifier.set("all")
        from(sourceSets.main.get().output)
        dependsOn(configurations.runtimeClasspath)
        from(configurations.runtimeClasspath.get().allDependencies.findAll( isDependency ).collect(collector))

        manifest {
            attributes(mapOf(
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to "New Relic, Inc.",
                    "Automatic-Module-Name" to "com.newrelic.telemetry"
            ))
        }
    }

}

