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

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

// region compare

internal fun File.compareFilesWith(file: File) {
    val inputDirectory = this
    val outputDirectory = file

    val inputFilesArray = inputDirectory.listFiles() ?: emptyArray()
    val inputFilesMap = inputFilesArray.associateBy { it.name }

    val outputFilesArray = outputDirectory.listFiles() ?: emptyArray()
    val outputFilesMap = outputFilesArray.associateBy { it.name }

    val keys = inputFilesMap.keys.union(outputFilesMap.keys)
    keys.forEach {
        val inputFile = inputFilesMap[it]
        val outputFile = outputFilesMap[it]

        when {
            inputFile == null -> {
                val file = inputDirectory.resolve(it)
                val message = buildString {
                    append("File not found")
                    append("\n")
                    append(file)
                }
                error(message)
            }
            outputFile == null -> {
                val file = outputDirectory.resolve(it)
                val message = buildString {
                    append("File not found")
                    append("\n")
                    append(file)
                }
                error(message)
            }
            else -> {
                val inputFileChecksum = inputFile.sha256()
                val outputFileChecksum = outputFile.sha256()
                if (inputFileChecksum != outputFileChecksum) {
                    val message = buildString {
                        append("The files are not the same")
                        append("\n")
                        append(inputFile)
                        append("\n")
                        append(outputFile)
                    }
                    error(message)
                }
            }
        }
    }
}

@Suppress("CheckedExceptionsKotlin")
private fun File.sha256(): String {
    return MessageDigest
        .getInstance("SHA-256")
        .update(this)
        .digest()
        .joinToString(separator = "") { "%02x".format(it) }
}

private fun MessageDigest.update(file: File): MessageDigest {
    return file
        .inputStream()
        .use { update(it) }
        .let { this }
}

@Suppress("CheckedExceptionsKotlin")
private fun MessageDigest.update(inputStream: InputStream) {
    val bytes = ByteArray(size = 1024)
    while (true) {
        val bytesCount = inputStream.read(bytes)
        if (bytesCount == -1) {
            break
        }

        update(bytes, 0, bytesCount)
    }
}

// endregion

// region copy

internal fun File.safeCopyTo(file: File) {
    val exists = exists()
    if (exists) {
        copyTo(file, overwrite = true)
    }
}

// endregion

// region pro files

internal fun File.apiFile(): File {
    return resolve(relative = "api.pro")
}

internal fun File.reflectionFile(): File {
    return resolve(relative = "reflection.pro")
}

internal fun File.settingsFile(): File {
    return resolve(relative = "settings.pro")
}

// endregion