
private object Versions {
    const val gson = "2.8.5"
    const val okhttp = "3.14.1"
    const val junit = "5.3.1"
    const val jsonassert = "1.5.0"
}

plugins {
    java
}

apply(plugin = "java")
apply(plugin = "java-library")

dependencies {
    "api"(project(":metrics"))
    "api"("com.google.code.gson:gson:${Versions.gson}")
    "api"("com.squareup.okhttp3:okhttp:${Versions.okhttp}")

    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
    testImplementation("org.skyscreamer:jsonassert:${Versions.jsonassert}")
}

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
}


