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

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import java.io.Writer

internal class KeepFileWriter(private val codeGenerator: CodeGenerator) {

    fun write(attributes: Collection<String>, classItems: Collection<KeepClassItem>, name: String, extension: String) {
        if (attributes.size == 0 && classItems.size == 0) {
            return
        }

        val writer = createWriter(name, extension)
        writer.use {
            it.writeAttributes(attributes)
            it.writeClassItems(classItems)
        }
    }

    private fun createWriter(name: String, extension: String): Writer {
        return codeGenerator
            .createNewFileByPath(Dependencies.ALL_FILES, name, extension)
            .writer(Charsets.UTF_8)
    }

    private fun Writer.writeAttributes(attributes: Collection<String>) {
        attributes.forEach { attribute ->
            writeText("-keepattributes")
            writeSpace()
            writeText(attribute)
            writeNewLine()
        }

        writeNewLine()
    }

    private fun Writer.writeClassItems(classItems: Collection<KeepClassItem>) {
        classItems.forEach { classItem ->
            val isEmpty = classItem.fields.isEmpty() && classItem.constructors.isEmpty() && classItem.functions.isEmpty()
            if (isEmpty) {
                writeText("-keep")
            } else {
                writeText("-keepclasseswithmembers")
            }
            writeSpace()
            writeText("class")
            writeSpace()
            writeText(classItem.name)
            if (!isEmpty) {
                writeSpace()
                writeCurlyBracketStart()
                classItem.fields.forEach { field ->
                    writeNewLine(count = 2)
                    writeTab()
                    writeText(field.type)
                    writeSpace()
                    writeText(field.name)
                    writeSemicolon()
                }
                classItem.constructors.forEach { constructor ->
                    writeNewLine(count = 2)
                    writeTab()
                    writeText("<init>")
                    writeParenthesesStart()
                    constructor.types.forEachIndexed { index, type ->
                        if (index > 0) {
                            writeComma()
                            writeSpace()
                        }
                        writeText(type)
                    }
                    writeParenthesesEnd()
                    writeSemicolon()
                }
                classItem.functions.forEach { function ->
                    writeNewLine(count = 2)
                    writeTab()
                    writeText(function.name)
                    writeParenthesesStart()
                    function.types.forEachIndexed { index, type ->
                        if (index > 0) {
                            writeComma()
                            writeSpace()
                        }
                        writeText(type)
                    }
                    writeParenthesesEnd()
                    writeSemicolon()
                }
                writeNewLine(count = 2)
                writeCurlyBracketEnd()
            }
            writeNewLine(count = 2)
        }
    }

    private fun Writer.writeCurlyBracketStart() {
        writeText("{")
    }

    private fun Writer.writeTab() {
        writeText("    ")
    }

    private fun Writer.writeParenthesesStart() {
        writeText("(")
    }

    private fun Writer.writeComma() {
        writeText(",")
    }

    private fun Writer.writeSpace() {
        writeText(" ")
    }

    private fun Writer.writeParenthesesEnd() {
        writeText(")")
    }

    private fun Writer.writeSemicolon() {
        writeText(";")
    }

    private fun Writer.writeCurlyBracketEnd() {
        writeText("}")
    }

    private fun Writer.writeNewLine(count: Int = 1) {
        repeat(count) { writeText("\n") }
    }

    @Suppress("CheckedExceptionsKotlin")
    private fun Writer.writeText(text: String) {
        write(text)
    }

}