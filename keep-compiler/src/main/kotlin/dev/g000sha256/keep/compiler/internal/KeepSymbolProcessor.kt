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

package dev.g000sha256.keep.compiler.internal

import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import java.util.concurrent.atomic.AtomicBoolean

internal class KeepSymbolProcessor(
    private val logger: KSPLogger,
    private val fileWriter: KeepFileWriter,
    private val symbolsMapper: KeepSymbolsMapper
) : SymbolProcessor {

    private val atomicBoolean = AtomicBoolean(false)

    override fun process(resolver: Resolver): List<KSAnnotated> {
        processOnce {
            processAPIAnnotation(resolver)
            processReflectionAnnotation(resolver)
        }
        return emptyList()
    }

    private inline fun processOnce(block: () -> Unit) {
        val success = atomicBoolean.compareAndSet(false, true)
        if (success) {
            block()
        }
    }

    private fun processAPIAnnotation(resolver: Resolver) {
        processAnnotation(
            resolver = resolver,
            annotationName = "dev.g000sha256.keep.annotation.api.KeepAPI",
            logTitle = "API classes",
            fileName = "api"
        )
    }

    private fun processReflectionAnnotation(resolver: Resolver) {
        processAnnotation(
            resolver = resolver,
            annotationName = "dev.g000sha256.keep.annotation.api.KeepReflection",
            logTitle = "Reflection classes",
            fileName = "reflection"
        )
    }

    private fun processAnnotation(resolver: Resolver, annotationName: String, logTitle: String, fileName: String) {
        val symbols = resolver.getSymbolsWithAnnotation(annotationName)
        val classItems = symbolsMapper.map(resolver, symbols)
        logClassItems(classItems, logTitle)
        fileWriter.write(classItems, fileName, extension = "pro")
    }

    private fun logClassItems(classItems: Sequence<KeepClassItem>, title: String) {
        logger.warn(title)
        classItems.forEach { classItem ->
            logger.warn("    class " + classItem.name)
            classItem.fields.forEach { logger.warn("        " + it.name + ": " + it.type) }
        }
    }

}