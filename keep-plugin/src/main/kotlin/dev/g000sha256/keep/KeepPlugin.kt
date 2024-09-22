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

import com.android.build.api.dsl.ApplicationExtension
import dev.g000sha256.keep_plugin.BuildConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.DependencyHandler

private const val ID_APPLICATION = "com.android.application"
private const val ID_LIBRARY = "com.android.library"

private const val GROUP = "dev.g000sha256"
private const val MODULE_API = "keep-annotation-api"
private const val MODULE_REFLECTION = "keep-annotation-reflection"
private const val VERSION_API = BuildConfig.VERSION_API
private const val VERSION_REFLECTION = BuildConfig.VERSION_REFLECTION

public class KeepPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val pluginContainer = target.plugins
        when {
            pluginContainer.hasPlugin(ID_APPLICATION) -> target.initAndroidApplication()
            pluginContainer.hasPlugin(ID_LIBRARY) -> target.initAndroidLibrary()
        }
    }

    private fun Project.initAndroidApplication() {
        dependencies.implementation(GROUP, MODULE_REFLECTION, VERSION_REFLECTION)

        extensions.configure(ApplicationExtension::class.java) { it.ignoreR8RulesFrom(GROUP, MODULE_API) }
    }

    private fun Project.initAndroidLibrary() {
        val dependencyHandler = dependencies
        dependencyHandler.implementation(GROUP, MODULE_API, VERSION_API)
        dependencyHandler.implementation(GROUP, MODULE_REFLECTION, VERSION_REFLECTION)
    }

    private fun DependencyHandler.implementation(group: String, module: String, version: String) {
        add("implementation", "$group:$module:$version")
    }

    private fun ApplicationExtension.ignoreR8RulesFrom(group: String, module: String) {
        @Suppress("UnstableApiUsage")
        defaultConfig.optimization.keepRules { ignoreFrom("$group:$module") }
    }

}