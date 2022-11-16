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
    api("io.ktor:ktor-client-core:$KTOR_VERSION")
    api("io.ktor:ktor-client-cio:$KTOR_VERSION")
    implementation("androidx.activity:activity-compose:$ANDROIDX_ACTIVITY_COMPOSE_VERSION")
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
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
}
