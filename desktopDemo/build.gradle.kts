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
//        compilations.all {
//            kotlinOptions.jvmTarget = java_version.toString()
//        }
        withJava()
    }
    sourceSets {
        println(map { it.name })
        val jvmMain by getting {
            dependencies {
                implementation(project(":mapview"))
                implementation(compose.desktop.currentOs)
                implementation("io.ktor:ktor-client-core:$ktor_version")
                implementation("io.ktor:ktor-client-cio:$ktor_version")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$serialization_version")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serialization_version")
            }
        }
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
