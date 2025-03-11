/*
 * Copyright 2025 Georgii Ippolitov (g000sha256)
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

package dev.g000sha256.keep.internal

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.internal.dsl.BuildType
import com.google.devtools.ksp.gradle.KspExtension
import com.google.devtools.ksp.gradle.KspGradleSubplugin
import dev.g000sha256.keep.KeepApplicationExtension
import dev.g000sha256.keep.KeepLibraryExtension
import dev.g000sha256.keep_plugin.BuildConfig
import java.io.File
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.plugins.ExtensionContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider

// region dependencies

internal fun Project.addImplementationApiDependency() {
    val dependency = buildDependency(module = "keep-annotation-api", version = BuildConfig.VERSION_ANNOTATION_API)
    dependencies.addImplementationDependency(dependency)
}

internal fun Project.addImplementationReflectionDependency() {
    val dependency = buildDependency(module = "keep-annotation-reflection", version = BuildConfig.VERSION_ANNOTATION_REFLECTION)
    dependencies.addImplementationDependency(dependency)
}

internal fun Project.addKspCompilerDependency() {
    val dependency = buildDependency(module = "keep-compiler", version = BuildConfig.VERSION_COMPILER)
    dependencies.addKspDependency(dependency)
}

private fun buildDependency(module: String, version: String): String {
    return "dev.g000sha256" + ":" + module + ":" + version
}

private fun DependencyHandler.addImplementationDependency(dependency: String) {
    add("implementation", dependency)
}

private fun DependencyHandler.addKspDependency(dependency: String) {
    add("ksp", dependency)
}

// endregion

// region extensions

internal fun Project.configureKspExtension(block: (KspExtension) -> Unit) {
    extensions.configureExtension(block)
}

internal fun Project.createKeepApplicationExtension(): KeepApplicationExtension {
    return extensions.createKeepExtension()
}

internal fun Project.createKeepLibraryExtension(): KeepLibraryExtension {
    return extensions.createKeepExtension()
}

private fun Project.configureAndroidApplicationExtension(block: (AppExtension) -> Unit) {
    extensions.configureExtension(block)
}

private fun Project.configureAndroidLibraryExtension(block: (LibraryExtension) -> Unit) {
    extensions.configureExtension(block)
}

private inline fun <reified T> ExtensionContainer.configureExtension(noinline block: (T) -> Unit) {
    configure(T::class.java, block)
}

private inline fun <reified T> ExtensionContainer.createKeepExtension(): T {
    return create("keep", T::class.java)
}

// region android

internal fun Project.androidApplicationVariants(block: Function2<String, BuildType, Unit>) {
    configureAndroidApplicationExtension { extension ->
        extension.applicationVariants.all {
            if (it.buildType.isMinifyEnabled) {
                val buildType = extension.buildTypes.getByName(it.buildType.name)
                block.invoke(it.name, buildType)
            }
        }
    }
}

internal fun Project.androidLibraryVariants(block: Function2<String, BuildType, Unit>) {
    configureAndroidLibraryExtension { extension ->
        extension.libraryVariants.all {
            if (it.buildType.isMinifyEnabled) {
                val buildType = extension.buildTypes.getByName(it.buildType.name)
                block.invoke(it.name, buildType)
            }
        }
    }
}

// endregion

// endregion

// region files

internal fun Project.getKeepDirectory(): File {
    return projectDir.resolve(relative = "keep")
}

internal fun Project.getKspDirectory(variantName: String): File {
    return projectDir.resolve(relative = "build/generated/ksp/" + variantName + "/resources")
}

// endregion

// region plugins

internal fun Project.applyKspGradleSubPlugin() {
    pluginManager.apply(KspGradleSubplugin::class.java)
}

internal fun Project.hasAndroidApplicationPlugin(): Boolean {
    return plugins.hasPlugin("com.android.application")
}

internal fun Project.hasAndroidLibraryPlugin(): Boolean {
    return plugins.hasPlugin("com.android.library")
}

// endregion

// region tasks

internal fun Project.registerKeepCheckTask(): TaskProvider<Task> {
    return tasks.registerKeepCheckTask()
}

internal fun Project.registerKeepGenerateTask(): TaskProvider<Task> {
    return tasks.registerKeepGenerateTask()
}

internal fun Project.registerKeepCheckTask(variantName: String, doLast: () -> Unit): TaskProvider<Task> {
    return tasks.registerKeepCheckTask(variantName, doLast)
}

internal fun Project.registerKeepGenerateTask(variantName: String, doLast: () -> Unit): TaskProvider<Task> {
    return tasks.registerKeepGenerateTask(variantName, doLast)
}

private val String.capitalized: String
    get() = replaceFirstChar { it.uppercaseChar() }

private val String?.orEmpty: String
    get() = orEmpty()

private fun TaskContainer.registerKeepCheckTask(
    variantName: String? = null,
    doLast: Function0<Unit>? = null
): TaskProvider<Task> {
    return registerKeepTask(taskName = "keepCheck", variantName, doLast)
}

private fun TaskContainer.registerKeepGenerateTask(
    variantName: String? = null,
    doLast: Function0<Unit>? = null
): TaskProvider<Task> {
    return registerKeepTask(taskName = "keepGenerate", variantName, doLast)
}

private fun TaskContainer.registerKeepTask(
    taskName: String,
    variantName: String? = null,
    doLast: Function0<Unit>? = null
): TaskProvider<Task> {
    val capitalizedVariantName = variantName?.capitalized
    return register(taskName + capitalizedVariantName.orEmpty) {
        it.group = "keep"

        if (capitalizedVariantName != null) {
            it.dependsOn("ksp" + capitalizedVariantName + "Kotlin")
        }

        if (doLast != null) {
            it.doLast { doLast() }
        }
    }
}

// endregion