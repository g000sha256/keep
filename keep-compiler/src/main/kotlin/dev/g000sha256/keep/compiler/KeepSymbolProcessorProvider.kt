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

package dev.g000sha256.keep.compiler

import com.google.auto.service.AutoService
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import dev.g000sha256.keep.compiler.internal.KeepFileWriter
import dev.g000sha256.keep.compiler.internal.KeepSymbolProcessor
import dev.g000sha256.keep.compiler.internal.KeepSymbolsMapper

@AutoService(SymbolProcessorProvider::class)
public class KeepSymbolProcessorProvider public constructor() : SymbolProcessorProvider {

    override fun create(environment: SymbolProcessorEnvironment): SymbolProcessor {
        val logger = environment.logger
        val fileWriter = KeepFileWriter(environment.codeGenerator)
        val symbolsMapper = KeepSymbolsMapper(logger)
        return KeepSymbolProcessor(logger, fileWriter, symbolsMapper)
    }

}