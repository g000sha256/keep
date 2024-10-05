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

internal class KeepClassItem(
    val name: String,
    val fields: Set<Field>,
    val constructors: Set<Constructor>,
    val functions: Set<Function>
) {

    class Field(val name: String, val type: String) {

        override fun equals(other: Any?): Boolean {
            if (other === this) {
                return true
            }

            if (other !is Field) {
                return false
            }

            return other.name == name && other.type == type
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + type.hashCode()
            return result
        }

    }

    class Constructor(val name: String, val types: Collection<String>) {

        override fun equals(other: Any?): Boolean {
            if (other === this) {
                return true
            }

            if (other !is Constructor) {
                return false
            }

            return other.name == name && other.types == types
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + types.hashCode()
            return result
        }

    }

    class Function(val name: String, val types: Collection<String>) {

        override fun equals(other: Any?): Boolean {
            if (other === this) {
                return true
            }

            if (other !is Function) {
                return false
            }

            return other.name == name && other.types == types
        }

        override fun hashCode(): Int {
            var result = name.hashCode()
            result = 31 * result + types.hashCode()
            return result
        }

    }

}