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

package dev.g000sha256.keep

import com.android.build.gradle.internal.dsl.BuildType
import com.android.build.gradle.internal.tasks.factory.dependsOn
import dev.g000sha256.keep.internal.addConsumerProFiles
import dev.g000sha256.keep.internal.addImplementationApiDependency
import dev.g000sha256.keep.internal.addImplementationReflectionDependency
import dev.g000sha256.keep.internal.addKspCompilerDependency
import dev.g000sha256.keep.internal.addMainProFiles
import dev.g000sha256.keep.internal.androidApplicationVariants
import dev.g000sha256.keep.internal.androidLibraryVariants
import dev.g000sha256.keep.internal.apiFile
import dev.g000sha256.keep.internal.applyKspGradleSubPlugin
import dev.g000sha256.keep.internal.compareFilesWith
import dev.g000sha256.keep.internal.configureKspExtension
import dev.g000sha256.keep.internal.createKeepApplicationExtension
import dev.g000sha256.keep.internal.createKeepLibraryExtension
import dev.g000sha256.keep.internal.getKeepDirectory
import dev.g000sha256.keep.internal.getKspDirectory
import dev.g000sha256.keep.internal.getOrElse
import dev.g000sha256.keep.internal.hasAndroidApplicationPlugin
import dev.g000sha256.keep.internal.hasAndroidLibraryPlugin
import dev.g000sha256.keep.internal.reflectionFile
import dev.g000sha256.keep.internal.registerKeepCheckTask
import dev.g000sha256.keep.internal.registerKeepGenerateTask
import dev.g000sha256.keep.internal.safeCopyTo
import dev.g000sha256.keep.internal.setTypeArgument
import dev.g000sha256.keep.internal.settingsFile
import java.io.File
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskProvider

public class KeepPlugin public constructor() : Plugin<Project> {

    override fun apply(target: Project) {
        when {
            target.hasAndroidApplicationPlugin() -> target.initAndroidApplication()
            target.hasAndroidLibraryPlugin() -> target.initAndroidLibrary()
            else -> error("The Keep plugin is compatible only with Android projects")
        }
    }

    private fun Project.initAndroidApplication() {
        val applicationExtension = createKeepApplicationExtension()

        initAndroid(
            outputDirectoryProvider = applicationExtension.outputDirectory,
            typeProvider = provider { KeepLibraryExtension.Type.ReflectionOnly },
            configureAndroidExtension = { androidApplicationVariants(it) }
        )
    }

    private fun Project.initAndroidLibrary() {
        val libraryExtension = createKeepLibraryExtension()

        initAndroid(
            outputDirectoryProvider = libraryExtension.outputDirectory,
            typeProvider = libraryExtension.type.orElse(KeepLibraryExtension.Type.All),
            configureAndroidExtension = { androidLibraryVariants(it) }
        )
    }

    private fun Project.initAndroid(
        outputDirectoryProvider: Provider<File>,
        typeProvider: Provider<KeepLibraryExtension.Type>,
        configureAndroidExtension: Function1<Function2<String, BuildType, Unit>, Unit>
    ) {
        applyKspGradleSubPlugin()

        initDependencies(typeProvider)

        configureKspExtension { it.setTypeArgument(typeProvider) }

        val rootKeepCheckTask = registerKeepCheckTask()
        val rootKeepGenerateTask = registerKeepGenerateTask()

        configureAndroidExtension.invoke { variantName, buildType ->
            onAndroidVariant(outputDirectoryProvider, rootKeepCheckTask, rootKeepGenerateTask, variantName, buildType)
        }
    }

    private fun Project.initDependencies(typeProvider: Provider<KeepLibraryExtension.Type>) {
        addKspCompilerDependency()

        if (typeProvider.isPresent) {
            initAnnotationDependencies(typeProvider)
        } else {
            afterEvaluate { initAnnotationDependencies(typeProvider) }
        }
    }

    private fun Project.initAnnotationDependencies(typeProvider: Provider<KeepLibraryExtension.Type>) {
        val type = typeProvider.get()
        when (type) {
            KeepLibraryExtension.Type.All -> {
                addImplementationApiDependency()
                addImplementationReflectionDependency()
            }
            KeepLibraryExtension.Type.ApiOnly -> addImplementationApiDependency()
            KeepLibraryExtension.Type.ReflectionOnly -> addImplementationReflectionDependency()
        }
    }

    private fun Project.onAndroidVariant(
        outputDirectoryProvider: Provider<File>,
        rootKeepCheckTask: TaskProvider<Task>,
        rootKeepGenerateTask: TaskProvider<Task>,
        variantName: String,
        buildType: BuildType
    ) {
        val inputDirectory = getKspDirectory(variantName)

        val outputDirectory = outputDirectoryProvider
            .getOrElse { getKeepDirectory() }
            .resolve(variantName)

        val apiOutputFile = outputDirectory.apiFile()
        val reflectionOutputFile = outputDirectory.reflectionFile()
        val settingsOutputFile = outputDirectory.settingsFile()

        buildType.addConsumerProFiles(reflectionOutputFile, settingsOutputFile)
        buildType.addMainProFiles(apiOutputFile, reflectionOutputFile, settingsOutputFile)

        val localKeepCheckTask = registerKeepCheckTask(variantName) {
            inputDirectory.compareFilesWith(outputDirectory)
        }
        rootKeepCheckTask.dependsOn(localKeepCheckTask)

        val localKeepGenerateTask = registerKeepGenerateTask(variantName) {
            inputDirectory
                .apiFile()
                .safeCopyTo(apiOutputFile)

            inputDirectory
                .reflectionFile()
                .safeCopyTo(reflectionOutputFile)

            inputDirectory
                .settingsFile()
                .safeCopyTo(settingsOutputFile)
        }
        rootKeepGenerateTask.dependsOn(localKeepGenerateTask)
    }

}