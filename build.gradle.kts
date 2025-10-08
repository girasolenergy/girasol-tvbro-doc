import org.jetbrains.kotlin.gradle.dsl.JsModuleKind

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
            moduleName.set(rootProject.name)
        }
    }

    sourceSets {
        val jsMain by getting {
            resources.srcDir("src/main/resources")
        }
    }
}
