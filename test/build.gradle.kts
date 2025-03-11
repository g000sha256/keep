import dev.g000sha256.keep.KeepLibraryExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(catalog.plugins.android.library)
    alias(catalog.plugins.jetBrains.binaryCompatibilityValidator)
    alias(catalog.plugins.jetBrains.kotlin.android)
    id("dev.g000sha256.keep") version "1.0.0"
}

android {
    buildToolsVersion = "35.0.1"
    compileSdk = 35
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
    implementation(catalog.jetBrains.annotations)
    implementation(catalog.jetBrains.kotlin)
}

keep {
    outputDirectory = File(projectDir, "proguard")
    type = KeepLibraryExtension.Type.All
}