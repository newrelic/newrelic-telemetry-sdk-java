
private object Versions {
    const val junit = "5.3.1"
    const val mockito = "2.23.0"
    const val slf4j = "1.7.26"
}

plugins {
    java
}

apply(plugin = "java-library")

dependencies {
    "api"(project(":metrics"))

    testImplementation("org.slf4j:slf4j-simple:${Versions.slf4j}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
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

