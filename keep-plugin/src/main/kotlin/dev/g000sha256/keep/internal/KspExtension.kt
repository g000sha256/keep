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

import com.google.devtools.ksp.gradle.KspExtension
import dev.g000sha256.keep.KeepLibraryExtension
import org.gradle.api.provider.Provider

internal fun KspExtension.setTypeArgument(provider: Provider<KeepLibraryExtension.Type>) {
    provider
        .map { it.mapToString() }
        .also { arg(k = "dev.g000sha256.keep.type", it) }
}

private fun KeepLibraryExtension.Type.mapToString(): String {
    when (this) {
        KeepLibraryExtension.Type.All -> return "all"
        KeepLibraryExtension.Type.ApiOnly -> return "api"
        KeepLibraryExtension.Type.ReflectionOnly -> return "reflection"
    }
}