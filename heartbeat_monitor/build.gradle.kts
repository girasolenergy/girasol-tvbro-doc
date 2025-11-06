import org.jetbrains.kotlin.gradle.dsl.JsModuleKind
import org.jetbrains.kotlin.gradle.dsl.JsSourceMapEmbedMode

plugins {
    kotlin("multiplatform") version "2.2.20"
}

repositories {
    mavenCentral()
}

kotlin {
    js {
        browser()
        binaries.executable()
        compilerOptions {
            moduleKind.set(JsModuleKind.MODULE_ES)
            moduleName.set("heartbeat_monitor")
            sourceMapEmbedSources = JsSourceMapEmbedMode.SOURCE_MAP_SOURCE_CONTENT_ALWAYS
            freeCompilerArgs.add("-Xcontext-parameters")
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
            }
        }
        val jsMain by getting {
            resources.srcDir("src/main/resources")
            resources.exclude("**/*.pdn")
            dependencies {
                implementation(npm("firebase", "12.0.0"))
            }
        }
    }
}
