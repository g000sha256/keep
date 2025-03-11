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

package dev.g000sha256.keep.compiler.internal

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import dev.g000sha256.keep.annotation.api.KeepApi
import dev.g000sha256.keep.annotation.reflection.KeepReflection
import java.util.concurrent.atomic.AtomicBoolean

internal class KeepSymbolProcessor(
    private val fileWriter: KeepFileWriter,
    private val symbolsMapper: KeepSymbolsMapper,
    private val options: Map<String, String>
) : SymbolProcessor {

    private val atomicBoolean = AtomicBoolean(false)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        processOnce {
            val keepResolver = KeepResolver(resolver)

            val type = options.get("dev.g000sha256.keep.type")
            when (type) {
                "all" -> {
                    processApiAnnotation(keepResolver)
                    processReflectionAnnotation(keepResolver)
                    writeReflectionSettings()
                }
                "api" -> {
                    processApiAnnotation(keepResolver)
                }
                "reflection" -> {
                    processReflectionAnnotation(keepResolver)
                    writeReflectionSettings()
                }
            }
        }

        return emptyList()
    }

    private inline fun processOnce(block: () -> Unit) {
        val success = atomicBoolean.compareAndSet(false, true)
        if (success) {
            block()
        }
    }

    private fun processApiAnnotation(resolver: KeepResolver) {
        resolver
            .getSymbolsWithAnnotation(KeepApi::class)
            .let { symbolsMapper.map(resolver, it) }
            .also { fileWriter.write(it.attributes, it.classItems, name = "api", extension = "pro") }
    }

    private fun processReflectionAnnotation(resolver: KeepResolver) {
        resolver
            .getSymbolsWithAnnotation(KeepReflection::class)
            .let { symbolsMapper.map(resolver, it) }
            .also { fileWriter.write(it.attributes, it.classItems, name = "reflection", extension = "pro") }
    }

    private fun writeReflectionSettings() {
        val attributes = setOf("RuntimeInvisibleAnnotations")
        val classItem = KeepClassItem(
            name = KeepReflection::class.java.name,
            fields = emptySet(),
            constructors = emptySet(),
            functions = emptySet()
        )
        val classItems = setOf(classItem)
        fileWriter.write(attributes, classItems, name = "settings", extension = "pro")
    }

}