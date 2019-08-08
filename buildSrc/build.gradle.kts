buildscript {
    dependencies {
        classpath ("com.bmuschko:gradle-nexus-plugin:2.3.1")
    }
}

repositories {
    jcenter()
}

apply(plugin = "com.bmuschko.nexus")

plugins {
    `kotlin-dsl`
}


dependencies {
    "api"("com.bmuschko:gradle-nexus-plugin:2.3.1")
}
