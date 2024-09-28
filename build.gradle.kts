plugins {
    alias(catalog.plugins.android.library) apply false
    alias(catalog.plugins.g000sha256.sonatypeMavenCentral) apply false
    alias(catalog.plugins.gmazzo.buildConfig) apply false
    alias(catalog.plugins.google.ksp) apply false
    alias(catalog.plugins.jetbrains.binaryCompatibilityValidator) apply false
    alias(catalog.plugins.jetbrains.kotlin.android) apply false
    alias(catalog.plugins.jetbrains.kotlin.jvm) apply false
    alias(catalog.plugins.jetbrains.kotlin.kapt) apply false
}