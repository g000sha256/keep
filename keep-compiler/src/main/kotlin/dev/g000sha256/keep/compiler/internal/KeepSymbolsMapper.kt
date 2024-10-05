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

internal class KeepSymbolsMapper(private val logger: KSPLogger) {

    fun map(resolver: Resolver, symbols: Sequence<KSAnnotated>): Collection<KeepClassItem> {
        val map = sortedMapOf<String, KeepClassItem>()
        symbols.forEach { symbol ->
            val classItems = mapClassItems(symbol)
            classItems.forEach { classItem ->
                val key = classItem.name
                val currentClassItem = map.get(key)
                if (currentClassItem == null) {
                    map.put(key, classItem)
                } else {
                    val mergedClassItem = KeepClassItem(
                        name = currentClassItem.name,
                        fields = currentClassItem.fields + classItem.fields,
                        constructors = currentClassItem.constructors + classItem.constructors,
                        functions = currentClassItem.functions + classItem.functions
                    )
                    map.put(key, mergedClassItem)
                }
            }
        }
        return map.values
    }

    private fun mapClassItems(symbol: KSAnnotated): Collection<KeepClassItem> {
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

    private fun KSClassDeclaration.getClassItems(): Collection<KeepClassItem> {
        return findClassItems(this)
    }

    private fun KSFile.getClassItems(): Collection<KeepClassItem> {
        return findClassItems(this)
    }

    private fun KSFunctionDeclaration.getClassItems(): Collection<KeepClassItem> {
        val parentNode = parent
        when (parentNode) {
            is KSClassDeclaration -> return findClassItems(parentNode)
            is KSFile -> return findClassItems(parentNode)
            else -> unknownDeclarationError()
        }
    }

    private fun KSPropertyDeclaration.getClassItems(): Collection<KeepClassItem> {
        val parentNode = parent
        when (parentNode) {
            is KSClassDeclaration -> return findClassItems(parentNode)
            is KSFile -> return findClassItems(parentNode)
            else -> unknownDeclarationError()
        }
    }

    private fun KSPropertyGetter.getClassItems(): Collection<KeepClassItem> {
        val parentNode = parent
        when (parentNode) {
            is KSClassDeclaration -> return findClassItems(parentNode)
            is KSFile -> return findClassItems(parentNode)
            else -> unknownDeclarationError()
        }
    }

    private fun KSPropertySetter.getClassItems(): Collection<KeepClassItem> {
        val parentNode = parent
        when (parentNode) {
            is KSClassDeclaration -> return findClassItems(parentNode)
            is KSFile -> return findClassItems(parentNode)
            else -> unknownDeclarationError()
        }
    }

    private fun KSTypeAlias.getClassItems(): Collection<KeepClassItem> {
        val parentNode = parent
        when (parentNode) {
            is KSFile -> return findClassItems(parentNode)
            else -> unknownDeclarationError()
        }
    }

    private fun findClassItems(classDeclaration: KSClassDeclaration): Collection<KeepClassItem> {
        val classItems = mutableListOf<KeepClassItem>()

        var currentClassDeclaration = classDeclaration
        var innerCompanionClassName: String? = null
        while (true) {
            val classType = getClassType(currentClassDeclaration)

            val classItem = createClassItem(currentClassDeclaration, classType, innerCompanionClassName)
            if (classItem != null) {
                classItems.add(classItem)
            }

            val parentNode = currentClassDeclaration.parent
            if (parentNode !is KSClassDeclaration) {
                break
            }

            innerCompanionClassName = getInnerCompanionClassName(classType, classItem)
            currentClassDeclaration = parentNode
        }

        return classItems
    }

    private fun createClassItem(
        classDeclaration: KSClassDeclaration,
        classType: ClassType,
        innerCompanionClassName: String?
    ): KeepClassItem? {
        val qualifiedName = getQualifiedName(classDeclaration)

        if (classType is ClassType.EnumEntry) {
            return null
        }

        if (classType is ClassType.Object) {
            return createClassItem(className = qualifiedName, fieldName = "INSTANCE", fieldType = qualifiedName)
        }

        if (innerCompanionClassName == null) {
            return createClassItem(qualifiedName)
        }

        val fieldName = innerCompanionClassName.substringAfterLast(delimiter = "$")
        val fieldType = innerCompanionClassName
        return createClassItem(className = qualifiedName, fieldName, fieldType)
    }

    private fun getInnerCompanionClassName(classType: ClassType, classItem: KeepClassItem?): String? {
        if (classItem == null) {
            return null
        }

        if (classType is ClassType.CompanionObject) {
            return classItem.name
        }

        return null
    }

    private fun findClassItems(file: KSFile): Collection<KeepClassItem> {
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

    private fun getQualifiedName(classDeclaration: KSClassDeclaration): String {
        val classNames = mutableListOf<String>()

        var currentClassDeclaration = classDeclaration
        while (true) {
            val simpleName = currentClassDeclaration.simpleName.asString()
            classNames.add(0, simpleName)

            val parentNode = currentClassDeclaration.parent
            if (parentNode !is KSClassDeclaration) {
                break
            }

            currentClassDeclaration = parentNode
        }

        val packageName = classDeclaration.packageName.asString()
        return classNames.joinToString(separator = "$", prefix = packageName + ".")
    }

    private fun getClassType(classDeclaration: KSClassDeclaration): ClassType {
        when (classDeclaration.classKind) {
            ClassKind.ENUM_ENTRY -> return ClassType.EnumEntry()
            ClassKind.OBJECT -> {
                if (classDeclaration.isCompanionObject) {
                    return ClassType.CompanionObject()
                }
                return ClassType.Object()
            }
            else -> return ClassType.Class()
        }
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

    private sealed interface ClassType {

        class Class : ClassType

        class CompanionObject : ClassType

        class EnumEntry : ClassType

        class Object : ClassType

    }

}