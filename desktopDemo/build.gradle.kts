import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    kotlin("plugin.serialization")
}

group = "mapview"
version = "1.0-SNAPSHOT"


kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = JAVA_VERSION.toString()
        }
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":mapview"))
                implementation(compose.desktop.currentOs)
                api("io.ktor:ktor-client-core:$KTOR_VERSION")
                api("io.ktor:ktor-client-cio:$KTOR_VERSION")
                api("org.jetbrains.kotlinx:kotlinx-serialization-core:$SERIALIZATION_VERSION")
                api("org.jetbrains.kotlinx:kotlinx-serialization-json:$SERIALIZATION_VERSION")
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "mapview"
            packageVersion = "1.0.0"
        }
    }
}
