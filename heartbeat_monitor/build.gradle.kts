import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode

plugins {
    kotlin("multiplatform") version "2.2.20"
}

repositories {
    mavenCentral()
    maven("https://raw.githubusercontent.com/MirrgieRiana/mirrg.kotlin/refs/heads/maven/maven/")
}

kotlin {
    js {
        browser()
        binaries.library()
        outputModuleName = "heartbeat-monitor"
        compilerOptions {
            moduleKind = JsModuleKind.MODULE_ES
            sourceMapEmbedSources = JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_ALWAYS
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
                implementation("mirrg.kotlin:mirrg.kotlin.helium-kotlin-2-2:4.2.0")
            }
        }
        jsMain {
            resources.srcDir("src/main/resources")
            resources.exclude("**/*.pdn")
            dependencies {
                implementation(npm("firebase", "12.0.0"))
            }
        }
    }
}
