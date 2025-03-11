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

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyAccessor
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import kotlin.reflect.KClass

internal class KeepResolver(private val resolver: Resolver) {

    fun <T : Annotation> getSymbolsWithAnnotation(klass: KClass<T>): Collection<KSAnnotated> {
        return resolver
            .getSymbolsWithAnnotation(klass.java.name)
            .toList()
    }

    fun getJvmName(classDeclaration: KSClassDeclaration): String {
        val qualifiedName = classDeclaration.qualifiedName?.asString()
        checkNotNull(qualifiedName) { "qualifiedName is null" }

        val packageName = classDeclaration.packageName.asString()
        val isPackageNameEmpty = packageName.isEmpty()
        if (isPackageNameEmpty) {
            return qualifiedName.replace(oldValue = ".", newValue = "$")
        }

        return qualifiedName
            .removePrefix(prefix = packageName + ".")
            .replace(oldValue = ".", newValue = "$")
            .let { packageName + "." + it }
    }

    fun getJvmName(file: KSFile): String {
        val simpleName: String
        val annotation = file.getAnnotation<JvmName>()
        if (annotation == null) {
            simpleName = file
                .fileName
                .removeSuffix(suffix = ".kt")
                .let { it + "Kt" }
        } else {
            simpleName = annotation.name
        }

        val packageName = file.packageName.asString()
        val isPackageNameEmpty = packageName.isEmpty()
        if (isPackageNameEmpty) {
            return simpleName
        }

        return packageName + "." + simpleName
    }

    @OptIn(KspExperimental::class)
    fun getJvmName(functionDeclaration: KSFunctionDeclaration): String {
        val jvmName = resolver.getJvmName(functionDeclaration)
        checkNotNull(jvmName) { "jvmName is null" }

        return fixJvmName(jvmName)
    }

    @OptIn(KspExperimental::class)
    fun getJvmName(propertyAccessor: KSPropertyAccessor): String {
        val jvmName = resolver.getJvmName(propertyAccessor)
        checkNotNull(jvmName) { "jvmName is null" }

        return fixJvmName(jvmName)
    }

    @OptIn(KspExperimental::class)
    fun getOwnerJvmClassName(functionDeclaration: KSFunctionDeclaration): String {
        val ownerJvmClassName = resolver.getOwnerJvmClassName(functionDeclaration)
        checkNotNull(ownerJvmClassName) { "ownerJvmClassName is null" }

        return ownerJvmClassName
    }

    @OptIn(KspExperimental::class)
    fun getOwnerJvmClassName(propertyDeclaration: KSPropertyDeclaration): String {
        val ownerJvmClassName = resolver.getOwnerJvmClassName(propertyDeclaration)
        checkNotNull(ownerJvmClassName) { "ownerJvmClassName is null" }

        return ownerJvmClassName
    }

    @OptIn(KspExperimental::class)
    private inline fun <reified T : Annotation> KSAnnotated.getAnnotation(): T? {
        val annotations = getAnnotationsByType(T::class)
        return annotations.firstOrNull()
    }

    private fun fixJvmName(name: String): String {
        val hasValueClass = name.contains(other = "-")
        if (hasValueClass) {
            return name
                .substringBefore(delimiter = "-")
                .let { it + "*" }
        }

        return name
    }

}