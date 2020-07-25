private object Versions {
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

//plugins.withType<JavaPlugin>().configureEach {
//    configure<JavaPluginExtension> {
//        modularity.inferModulePath.set(true)
//    }
//}

tasks.register<Jar>("jarAll") {
    dependsOn(configurations.runtimeClasspath)
    from(sourceSets["main"].output)
    configurations.runtimeClasspath.get().allDependencies.findAll(closureOf<Any>{
        val proj = this as ProjectDependency
        from(proj.dependencyProject.sourceSets["main"].output)
    })
    manifest.attributes["Automatic-Module-Name"] = "com.newrelic.telemetry"
}
