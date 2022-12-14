plugins {
    id("org.jetbrains.compose")
    id("com.android.application")
    kotlin("android")
}

group "mapview"
version "1.0-SNAPSHOT"

repositories {
    jcenter()
}

dependencies {
    implementation(project(":mapview"))
    api("io.ktor:ktor-client-core:$ktor_version")
    api("io.ktor:ktor-client-cio:$ktor_version")
    implementation("androidx.activity:activity-compose:$androidx_activity_compose_version")
}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "mapview.android"
        minSdk = 26
        targetSdk = 32
        versionCode = 1
        versionName = "1.0-SNAPSHOT"
    }
    compileOptions {
        sourceCompatibility = java_version
        targetCompatibility = java_version
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}
