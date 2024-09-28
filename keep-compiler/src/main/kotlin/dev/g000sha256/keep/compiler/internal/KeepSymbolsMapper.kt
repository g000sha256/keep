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

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSPropertyGetter
import com.google.devtools.ksp.symbol.KSPropertySetter
import com.google.devtools.ksp.symbol.KSTypeAlias
import java.util.TreeMap

internal class KeepSymbolsMapper(private val logger: KSPLogger) {

    fun map(resolver: Resolver, symbols: Sequence<KSAnnotated>): Sequence<KeepClassItem> {
        val map = TreeMap<String, KeepClassItem>()
        symbols.forEach { symbol ->
            val classItems = mapClassItems(symbol)
            classItems.forEach { classItem ->
                val key = classItem.name
                val currentClassItem = map.get(key)
                if (currentClassItem == null) {
                    map.put(key, classItem)
                } else {
                    val mergedClassItem = currentClassItem.copy(fields = currentClassItem.fields + classItem.fields)
                    map.put(key, mergedClassItem)
                }
            }
        }
        return map.values.asSequence()
    }

    private fun mapClassItems(symbol: KSAnnotated): List<KeepClassItem> {
        when (symbol) {
            is KSClassDeclaration -> return symbol.getClassItems()
            is KSFile -> return symbol.getClassItems()
            is KSFunctionDeclaration -> return symbol.getClassItems()
            is KSPropertyDeclaration -> return symbol.getClassItems()
            is KSPropertyGetter -> return symbol.getClassItems()
            is KSPropertySetter -> return symbol.getClassItems()
            is KSTypeAlias -> return symbol.getClassItems()
            else -> unknownDeclarationError()
        }
    }

    private fun KSClassDeclaration.getClassItems(): List<KeepClassItem> {
        return findClassItems(this)
    }

    private fun KSFile.getClassItems(): List<KeepClassItem> {
        return findClassItems(this)
    }

    private fun KSFunctionDeclaration.getClassItems(): List<KeepClassItem> {
        val parentNode = parent
        when (parentNode) {
            is KSClassDeclaration -> return findClassItems(parentNode)
            is KSFile -> return findClassItems(parentNode)
            else -> unknownDeclarationError()
        }
    }

    private fun KSPropertyDeclaration.getClassItems(): List<KeepClassItem> {
        val parentNode = parent
        when (parentNode) {
            is KSClassDeclaration -> return findClassItems(parentNode)
            is KSFile -> return findClassItems(parentNode)
            else -> unknownDeclarationError()
        }
    }

    private fun KSPropertyGetter.getClassItems(): List<KeepClassItem> {
        val parentNode = parent
        when (parentNode) {
            is KSClassDeclaration -> return findClassItems(parentNode)
            is KSFile -> return findClassItems(parentNode)
            else -> unknownDeclarationError()
        }
    }

    private fun KSPropertySetter.getClassItems(): List<KeepClassItem> {
        val parentNode = parent
        when (parentNode) {
            is KSClassDeclaration -> return findClassItems(parentNode)
            is KSFile -> return findClassItems(parentNode)
            else -> unknownDeclarationError()
        }
    }

    private fun KSTypeAlias.getClassItems(): List<KeepClassItem> {
        val parentNode = parent
        when (parentNode) {
            is KSFile -> return findClassItems(parentNode)
            else -> unknownDeclarationError()
        }
    }

    private fun findClassItems(classDeclaration: KSClassDeclaration): List<KeepClassItem> {
        return getClasses(classDeclaration)
    }

    private fun findClassItems(file: KSFile): List<KeepClassItem> {
        val className = getClassName(file)
        val classItem = createClassItem(className)
        return listOf(classItem)
    }

    private fun getClassName(file: KSFile): String {
        val packageName = file.packageName.asString()
        val annotation = file.getJvmNameAnnotation()
        if (annotation == null) {
            return packageName + "." + file.fileName.replace(".kt", "Kt")
        }
        return packageName + "." + annotation.name
    }

    @OptIn(KspExperimental::class)
    private fun KSFile.getJvmNameAnnotation(): JvmName? {
        val annotations = getAnnotationsByType(JvmName::class)
        return annotations.firstOrNull()
    }

    private fun unknownDeclarationError(): Nothing {
        error(message = "Unknown declaration")
    }

    //////
    //////
    //////

    private fun getClasses(classDeclaration: KSClassDeclaration): List<KeepClassItem> {
        val classes = mutableListOf<KeepClassItem>()

        val classHierarchy = getClassHierarchy(classDeclaration)

        for (index in classHierarchy.indices) {
            val currentClassInfo = classHierarchy.get(index)
            val nextClassInfo = classHierarchy.getOrNull(index + 1)

            val qualifiedName = currentClassInfo.qualifiedName

            if (nextClassInfo != null) {
                if (nextClassInfo.type is ClassInfo.Type.Companion) {
                    val fieldType = qualifiedName + '$' + nextClassInfo.simpleName
                    classes += createClassItem(className = qualifiedName, fieldName = nextClassInfo.simpleName, fieldType)
                    continue
                }
            }

            if (currentClassInfo.type is ClassInfo.Type.Object) {
                classes += createClassItem(className = qualifiedName, fieldName = "INSTANCE", fieldType = qualifiedName)
                continue
            }

            classes += createClassItem(qualifiedName)
        }

        return classes
    }

    private fun getClassHierarchy(classDeclaration: KSClassDeclaration): List<ClassInfo> {
        val classes = mutableListOf<ClassInfo>()

        var currentClassDeclaration = classDeclaration
        while (true) {
            val info = getClassInfo(currentClassDeclaration)
            classes.add(0, info)

            val parentNode = currentClassDeclaration.parent
            if (parentNode !is KSClassDeclaration) {
                break
            }

            currentClassDeclaration = parentNode
        }

        return classes
    }

    private fun getClassInfo(classDeclaration: KSClassDeclaration): ClassInfo {
        val qualifiedName = getQualifiedName(classDeclaration)
        val simpleName = classDeclaration.simpleName.asString()
        val type = getClassType(classDeclaration)
        return ClassInfo(qualifiedName, simpleName, type)
    }

    private fun getQualifiedName(classDeclaration: KSClassDeclaration): String {
        val classes = mutableListOf<String>()

        var currentClassDeclaration = classDeclaration
        while (true) {
            val simpleName = currentClassDeclaration.simpleName.asString()
            classes.add(0, simpleName)

            val parentNode = currentClassDeclaration.parent
            if (parentNode !is KSClassDeclaration) {
                break
            }

            currentClassDeclaration = parentNode
        }

        val packageName = classDeclaration.packageName.asString()
        return classes.joinToString(separator = "$", prefix = packageName + ".")
    }

    private fun getClassType(classDeclaration: KSClassDeclaration): ClassInfo.Type {
        if (classDeclaration.classKind == ClassKind.OBJECT) {
            if (classDeclaration.isCompanionObject) {
                return ClassInfo.Type.Companion()
            }
            return ClassInfo.Type.Object()
        }
        return ClassInfo.Type.Class()
    }

    private fun createClassItem(className: String): KeepClassItem {
        return KeepClassItem(
            name = className,
            fields = emptySet(),
            constructors = emptySet(),
            functions = emptySet()
        )
    }

    private fun createClassItem(className: String, fieldName: String, fieldType: String): KeepClassItem {
        val field = KeepClassItem.Field(fieldName, fieldType)
        return KeepClassItem(
            name = className,
            fields = setOf(field),
            constructors = emptySet(),
            functions = emptySet()
        )
    }

    private class ClassInfo(val qualifiedName: String, val simpleName: String, val type: Type) {

        sealed interface Type {

            class Class : Type

            class Companion : Type

            class Object : Type

        }

    }

}