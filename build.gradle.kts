group "com.ginhub.lewik"
version "1.0-SNAPSHOT"

allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://maven.pkg.jetbrains.space/public/p/ktor/eap")
    }
}

plugins {
    kotlin("multiplatform").version(kotlin_version) apply (false)
    kotlin("plugin.serialization").version(kotlin_version) apply (false)
    kotlin("android").version(kotlin_version) apply (false)
    id("com.android.application").version(agp_version) apply (false)
    id("com.android.library").version(agp_version) apply (false)
    id("org.jetbrains.compose").version(compose_version) apply (false)
}

version = "1.0-SNAPSHOT"


