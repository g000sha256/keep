import com.google.devtools.ksp.gradle.KspTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(catalog.plugins.android.library)
    alias(catalog.plugins.google.ksp)
    alias(catalog.plugins.jetbrains.binaryCompatibilityValidator)
    alias(catalog.plugins.jetbrains.kotlin.android)
    id("dev.g000sha256.keep") version "0.0.1"
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

    ksp("dev.g000sha256:keep-compiler:0.0.2")
}

//////
//////
//////

afterEvaluate {
    android.libraryVariants.forEach { variant ->
        if (variant.buildType.isMinifyEnabled) {
            val variantName = variant.name
            val taskName = "ksp" + variantName.replaceFirstChar { it.uppercaseChar() } + "Kotlin"

            val task = tasks.getByName<KspTask>(taskName)
            task.doLast {
                val inputDir = File(buildDir, "generated/ksp")

                val outputDir = File(projectDir, "pro")
                outputDir.mkdirs()

                val inputFile = File(inputDir, variantName + "/resources/api.pro")
                val outputFile = File(outputDir, variantName + ".pro")
                copy(inputFile, outputFile)
            }
        }
    }
}

fun copy(input: File, output: File) {
    if (input.exists().not()) {
        return
    }
    input.copyTo(output, overwrite = true)
}