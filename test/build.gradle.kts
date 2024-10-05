import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(catalog.plugins.android.library)
    alias(catalog.plugins.jetbrains.binaryCompatibilityValidator)
    alias(catalog.plugins.jetbrains.kotlin.android)
    id("dev.g000sha256.keep") version "0.0.2"
}

android {
    buildToolsVersion = "34.0.0"
    compileSdk = 34
    namespace = "dev.g000sha256.keep.test"

    buildTypes {
        release { isMinifyEnabled = true }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    defaultConfig { minSdk = 21 }
}

kotlin {
    explicitApi()

    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
        moduleName = "dev.g000sha256.keep.test"
    }
}

dependencies {
    implementation(catalog.jetbrains.annotations)
    implementation(catalog.jetbrains.kotlin)
}