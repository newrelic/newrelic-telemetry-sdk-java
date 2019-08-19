private object Versions {
    const val lombok = "1.18.6"
    const val junit = "5.3.1"
    const val guava = "27.1-jre"
    const val mockito = "2.23.0"
    const val slf4j = "1.7.26"
}

plugins {
    java
}

apply(plugin = "java-library")

dependencies {
    "api"("org.slf4j:slf4j-api:${Versions.slf4j}")
    compileOnly("org.projectlombok:lombok:${Versions.lombok}")
    annotationProcessor("org.projectlombok:lombok:${Versions.lombok}")

    testImplementation("org.slf4j:slf4j-simple:${Versions.slf4j}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${Versions.junit}")
    testCompileOnly("org.projectlombok:lombok:${Versions.lombok}")
    testAnnotationProcessor("org.projectlombok:lombok:${Versions.lombok}")
    testImplementation("org.mockito:mockito-core:${Versions.mockito}")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${Versions.junit}")
    testImplementation("com.google.guava:guava:${Versions.guava}")
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

