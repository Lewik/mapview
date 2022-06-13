group "mapview"
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
    kotlin("multiplatform").version(KOTLIN_VERSION) apply (false)
    kotlin("android").version(KOTLIN_VERSION) apply (false)
    id("com.android.application").version(AGP_VERSION) apply (false)
    id("com.android.library").version(AGP_VERSION) apply (false)
    id("org.jetbrains.compose").version(COMPOSE_VERSION) apply (false)
}

version = "1.0-SNAPSHOT"


