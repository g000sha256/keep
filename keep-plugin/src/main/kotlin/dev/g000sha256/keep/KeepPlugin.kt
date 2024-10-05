/*
 * Copyright 2024 Georgii Ippolitov (g000sha256)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.g000sha256.keep

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import com.google.devtools.ksp.gradle.KspTask
import dev.g000sha256.keep_plugin.BuildConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler
import java.io.File

private const val PLUGIN_ID_APPLICATION = "com.android.application"
private const val PLUGIN_ID_LIBRARY = "com.android.library"

private const val GROUP = "dev.g000sha256"

private const val MODULE_ANNOTATION_API = "keep-annotation-api"
private const val MODULE_ANNOTATION_REFLECTION = "keep-annotation-reflection"
private const val MODULE_COMPILER = "keep-compiler"

private const val VERSION_ANNOTATION_API = BuildConfig.VERSION_ANNOTATION_API
private const val VERSION_ANNOTATION_REFLECTION = BuildConfig.VERSION_ANNOTATION_REFLECTION
private const val VERSION_COMPILER = BuildConfig.VERSION_COMPILER

private const val FILE_API = "api.pro"
private const val FILE_REFLECTION = "reflection.pro"

public class KeepPlugin public constructor() : Plugin<Project> {

    override fun apply(target: Project) {
        val pluginContainer = target.plugins
        when {
            pluginContainer.hasPlugin(PLUGIN_ID_APPLICATION) -> target.initAndroidApplication()
            pluginContainer.hasPlugin(PLUGIN_ID_LIBRARY) -> target.initAndroidLibrary()
        }
    }

    private fun Project.initAndroidApplication() {
        pluginManager.apply(KspGradleSubplugin::class.java)

        dependencies.also {
            it.implementation(GROUP, MODULE_ANNOTATION_REFLECTION, VERSION_ANNOTATION_REFLECTION)

            it.ksp(GROUP, MODULE_COMPILER, VERSION_COMPILER)
        }

        extensions.configure(AppExtension::class.java) { extension ->
            extension.ignoreR8RulesFrom(GROUP, MODULE_ANNOTATION_API)

            extension.applicationVariants.all { variant ->
                val variantName = variant.name
                val buildType = extension.buildTypes.getByName(variantName)

                if (buildType.isMinifyEnabled) {
                    val outputDirectory = createOutputDirectory(variantName)

                    val reflectionOutputFile = File(outputDirectory, FILE_REFLECTION)

                    buildType.proguardFile(reflectionOutputFile)
                    buildType.consumerProguardFile(reflectionOutputFile)

                    doAfterKspTask(variantName) {
                        outputDirectory.deleteFiles()

                        val reflectionInputFile = getInputFile(variantName, FILE_REFLECTION)
                        moveFile(reflectionInputFile, reflectionOutputFile)
                    }
                }
            }
        }
    }

    @Suppress("UnstableApiUsage")
    private fun AppExtension.ignoreR8RulesFrom(group: String, module: String) {
        defaultConfig.optimization.keepRules { ignoreFrom(group + ":" + module) }
    }

    private fun Project.initAndroidLibrary() {
        pluginManager.apply(KspGradleSubplugin::class.java)

        dependencies.also {
            it.implementation(GROUP, MODULE_ANNOTATION_API, VERSION_ANNOTATION_API)
            it.implementation(GROUP, MODULE_ANNOTATION_REFLECTION, VERSION_ANNOTATION_REFLECTION)

            it.ksp(GROUP, MODULE_COMPILER, VERSION_COMPILER)
        }

        extensions.configure(LibraryExtension::class.java) { extension ->
            extension.libraryVariants.all { variant ->
                val variantName = variant.name
                val buildType = extension.buildTypes.getByName(variantName)

                if (buildType.isMinifyEnabled) {
                    val outputDirectory = createOutputDirectory(variantName)

                    val apiOutputFile = File(outputDirectory, FILE_API)
                    val reflectionOutputFile = File(outputDirectory, FILE_REFLECTION)

                    buildType.proguardFiles(apiOutputFile, reflectionOutputFile)
                    buildType.consumerProguardFile(reflectionOutputFile)

                    doAfterKspTask(variantName) {
                        outputDirectory.deleteFiles()

                        val apiInputFile = getInputFile(variantName, FILE_API)
                        moveFile(apiInputFile, apiOutputFile)

                        val reflectionInputFile = getInputFile(variantName, FILE_REFLECTION)
                        moveFile(reflectionInputFile, reflectionOutputFile)
                    }
                }
            }
        }
    }

    private fun DependencyHandler.implementation(group: String, module: String, version: String) {
        add("implementation", group + ":" + module + ":" + version)
    }

    private fun DependencyHandler.ksp(group: String, module: String, version: String) {
        add("ksp", group + ":" + module + ":" + version)
    }

    private fun Project.createOutputDirectory(variantName: String): File {
        val file = File(projectDir, "pro/" + variantName)
        file.mkdirs()
        return file
    }

    private fun Project.doAfterKspTask(variantName: String, block: () -> Unit) {
        tasks.withType(KspTask::class.java) {
            val taskName = "ksp" + variantName.capitalize() + "Kotlin"
            if (name == taskName) {
                it.doLast { block() }
            }
        }
    }

    private fun String.capitalize(): String {
        return replaceFirstChar { it.uppercaseChar() }
    }

    private fun File.deleteFiles() {
        val files = listFiles()
        files?.forEach { it.deleteRecursively() }
    }

    private fun Project.getInputFile(variantName: String, fileName: String): File {
        return File(projectDir, "build/generated/ksp/" + variantName + "/resources/" + fileName)
    }

    private fun moveFile(inputFile: File, outputFile: File) {
        val fromFileExists = inputFile.exists()
        if (fromFileExists) {
            inputFile.copyTo(outputFile, overwrite = true)
            inputFile.delete()
        }
    }

}