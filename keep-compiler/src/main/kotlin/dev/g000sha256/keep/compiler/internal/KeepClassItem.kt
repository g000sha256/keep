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

internal data class KeepClassItem(
    val name: String,
    val fields: Set<Field>,
    val constructors: Set<Constructor>,
    val functions: Set<Function>
) {

    data class Field(val name: String, val type: String)

    data class Constructor(val name: String, val types: List<String>)

    data class Function(val name: String, val types: List<String>)

}