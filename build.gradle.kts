buildscript {
    dependencies {
        classpath("gradle.plugin.com.github.sherter.google-java-format:google-java-format-gradle-plugin:0.8")
    }
}

plugins {
    java
}

apply(plugin = "java")
apply(plugin = "java-library")
apply(plugin = "maven-publish")
apply(plugin = "signing")

apply(plugin = "com.github.sherter.google-java-format")

allprojects {
    group = "com.newrelic.telemetry"
    version = project.findProperty("releaseVersion") as String
    repositories {
        mavenCentral()
        jcenter()
    }
    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }
    tasks.register<Jar>("sourcesJar") {
        from(sourceSets.main.get().allJava)
        archiveClassifier.set("sources")
    }

    tasks.register<Jar>("javadocJar") {
        from(tasks.javadoc)
        archiveClassifier.set("javadoc")
    }

}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

//listOf(":metrics", ":telemetry", ":telemetry-components").forEach {
//    project(it) {
//        apply(plugin = "java-library")
//        tasks {
//            val taskScope = this
//            val sources = sourceSets
//            val sourcesJar by creating(Jar::class) {
//                dependsOn(JavaPlugin.CLASSES_TASK_NAME)
//                archiveClassifier.set("sources")
//                from(sources.main.get().allSource)
//            }
//
//            val javadocJar by creating(Jar::class) {
//                dependsOn(JavaPlugin.JAVADOC_TASK_NAME)
//                archiveClassifier.set("javadoc")
//                from(taskScope.javadoc)
//            }
//
//            artifacts {
//                add("archives", sourcesJar)
//                add("archives", javadocJar)
//            }
//        }
//    }
//}