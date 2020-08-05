private object Versions {
        const val slf4j = "1.7.30"
        const val gson = "2.8.6"
}

plugins {
    java
    id("java-library")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

sourceSets {
    main {
        java.setSrcDirs(listOf(
                "src/main/java",
                "../telemetry_core/src/main/java",
                "../telemetry/src/main/java",
                "../telemetry-http-java11/src/main/java"
        ))
        resources.setSrcDirs(listOf(
                "../telemetry_core/src/main/resources",
                "../telemetry/src/main/resources",
                "../telemetry-http-java11/src/main/resources"
        ))
    }
}
dependencies {
    api("org.slf4j:slf4j-api:${Versions.slf4j}")
    implementation("com.google.code.gson:gson:${Versions.gson}")
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
