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

import com.google.devtools.ksp.isConstructor
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSPropertyGetter
import com.google.devtools.ksp.symbol.KSPropertySetter
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.Modifier

internal class KeepSymbolsMapper(private val logger: KSPLogger) {

    fun map(resolver: KeepResolver, symbols: Collection<KSAnnotated>): KeepData {
        val classItemsList = symbols.map { symbol -> mapClassItems(resolver, symbol) }

        fun getAttributes(): Collection<String> {
            val hasInnerClasses = classItemsList.any { it.size > 1 }
            if (hasInnerClasses) {
                return buildSet {
                    add("EnclosingMethod")
                    add("InnerClasses")
                }
            }

            return emptySet()
        }

        fun getClassItems(): Collection<KeepClassItem> {
            if (classItemsList.size == 0) {
                return emptySet()
            }

            val classItems = mutableMapOf<String, KeepClassItem>()

            return classItemsList
                .flatten()
                .forEach { classItems.putOrMerge(it) }
                .let { classItems.values }
        }

        return KeepData(
            attributes = getAttributes(),
            classItems = getClassItems()
        )
    }

    private fun mapClassItems(resolver: KeepResolver, annotated: KSAnnotated): Collection<KeepClassItem> {
        when (annotated) {
            is KSClassDeclaration -> return annotated.getClassItems(resolver)
            is KSFile -> return annotated.getClassItems(resolver)
            is KSFunctionDeclaration -> return annotated.getClassItems(resolver)
            is KSPropertyDeclaration -> return annotated.getClassItems(resolver)
            is KSPropertyGetter -> return annotated.getClassItems(resolver)
            is KSPropertySetter -> return annotated.getClassItems(resolver)
            is KSTypeAlias -> return annotated.getClassItems(resolver)
            else -> {
                logger.error(message = "Unknown declaration", symbol = annotated)
                return emptyList()
            }
        }
    }

    private fun KSClassDeclaration.getClassItems(resolver: KeepResolver): Collection<KeepClassItem> {
        when (classKind) {
            ClassKind.ENUM_ENTRY -> {
                val name = simpleName.asString()
                val type = resolver.getJvmName(parent as KSClassDeclaration)
                val field = KeepClassItem.Field(name, type)
                return findClassItems(resolver, this, field = field, constructor = null, function = null)
            }
            else -> return findClassItems(resolver, this, field = null, constructor = null, function = null)
        }
    }

    private fun KSFile.getClassItems(resolver: KeepResolver): Collection<KeepClassItem> {
        val qualifiedName = resolver.getJvmName(this)
        val classItem = createClassItem(qualifiedName, field = null, constructor = null, function = null)
        return listOf(classItem)
    }

    private fun KSFunctionDeclaration.getClassItems(resolver: KeepResolver): Collection<KeepClassItem> {
        val parentNode = parent
        when (parentNode) {
            is KSClassDeclaration -> {
                val isConstructor = isConstructor()
                if (isConstructor) {
                    val types = listOf("...")
                    val constructor = KeepClassItem.Constructor(types)
                    return findClassItems(resolver, parentNode, field = null, constructor, function = null)
                }

                val name = resolver.getJvmName(this)

                val types: List<String>
                when {
                    extensionReceiver != null -> types = listOf("...")
                    parameters.size > 0 -> types = listOf("...")
                    modifiers.contains(Modifier.SUSPEND) -> types = listOf("...")
                    else -> types = emptyList()
                }

                val function = KeepClassItem.Function(name, types)
                return findClassItems(resolver, parentNode, field = null, constructor = null, function)
            }
            is KSFile -> {
                val qualifiedName = resolver.getOwnerJvmClassName(this)

                val name = resolver.getJvmName(this)

                val types: List<String>
                when {
                    extensionReceiver != null -> types = listOf("...")
                    parameters.size > 0 -> types = listOf("...")
                    modifiers.contains(Modifier.SUSPEND) -> types = listOf("...")
                    else -> types = emptyList()
                }

                val function = KeepClassItem.Function(name, types)
                val classItem = createClassItem(qualifiedName, field = null, constructor = null, function)
                return listOf(classItem)
            }
            else -> {
                logger.error(message = "Unknown parent declaration", symbol = parentNode)
                return emptyList()
            }
        }
    }

    private fun KSPropertyDeclaration.getClassItems(resolver: KeepResolver): Collection<KeepClassItem> {
        val parentNode = parent
        when (parentNode) {
            is KSClassDeclaration -> {
                when (parentNode.classKind) {
                    ClassKind.OBJECT -> {
                        val name = resolver.getJvmName(getter as KSPropertyGetter)
                        val types = emptyList<String>()
                        val function = KeepClassItem.Function(name, types)
                        return findClassItems(resolver, parentNode, field = null, constructor = null, function)
                    }
                    else -> {
                        val name = simpleName.asString()
                        val field = KeepClassItem.Field(name, type = "***")
                        return findClassItems(resolver, parentNode, field, constructor = null, function = null)
                    }
                }
            }
            is KSFile -> {
                val qualifiedName = resolver.getOwnerJvmClassName(this)
                val name = simpleName.asString()
                val field = KeepClassItem.Field(name, type = "***")
                val classItem = createClassItem(qualifiedName, field, constructor = null, function = null)
                return listOf(classItem)
            }
            else -> {
                logger.error(message = "Unknown parent declaration", symbol = parentNode)
                return emptyList()
            }
        }
    }

    private fun KSPropertyGetter.getClassItems(resolver: KeepResolver): Collection<KeepClassItem> {
        val parentNode = receiver.parent
        when (parentNode) {
            is KSClassDeclaration -> {
                val name = resolver.getJvmName(this)

                val types: List<String>
                when {
                    receiver.extensionReceiver != null -> types = listOf("...")
                    else -> types = emptyList()
                }

                val function = KeepClassItem.Function(name, types)
                return findClassItems(resolver, parentNode, field = null, constructor = null, function)
            }
            is KSFile -> {
                val qualifiedName = resolver.getOwnerJvmClassName(receiver)

                val name = resolver.getJvmName(this)

                val types: List<String>
                when {
                    receiver.extensionReceiver != null -> types = listOf("...")
                    else -> types = emptyList()
                }

                val function = KeepClassItem.Function(name, types)
                val classItem = createClassItem(qualifiedName, field = null, constructor = null, function)
                return listOf(classItem)
            }
            is KSFunctionDeclaration -> {
                val rootNode = parentNode.parent
                when (rootNode) {
                    is KSClassDeclaration -> {
                        val name = resolver.getJvmName(this)
                        val types = emptyList<String>()
                        val function = KeepClassItem.Function(name, types)
                        return findClassItems(resolver, rootNode, field = null, constructor = null, function)
                    }
                    else -> {
                        logger.error(message = "Unknown parent declaration", symbol = rootNode)
                        return emptyList()
                    }
                }
            }
            else -> {
                logger.error(message = "Unknown parent declaration", symbol = parentNode)
                return emptyList()
            }
        }
    }

    private fun KSPropertySetter.getClassItems(resolver: KeepResolver): Collection<KeepClassItem> {
        val parentNode = receiver.parent
        when (parentNode) {
            is KSClassDeclaration -> {
                val name = resolver.getJvmName(this)
                val types = listOf("...")
                val function = KeepClassItem.Function(name, types)
                return findClassItems(resolver, parentNode, field = null, constructor = null, function)
            }
            is KSFile -> {
                val qualifiedName = resolver.getOwnerJvmClassName(receiver)
                val name = resolver.getJvmName(this)
                val types = listOf("...")
                val function = KeepClassItem.Function(name, types)
                val classItem = createClassItem(qualifiedName, field = null, constructor = null, function)
                return listOf(classItem)
            }
            is KSFunctionDeclaration -> {
                val rootNode = parentNode.parent
                when (rootNode) {
                    is KSClassDeclaration -> {
                        val name = resolver.getJvmName(this)
                        val types = listOf("...")
                        val function = KeepClassItem.Function(name, types)
                        return findClassItems(resolver, rootNode, field = null, constructor = null, function)
                    }
                    else -> {
                        logger.error(message = "Unknown parent declaration", symbol = rootNode)
                        return emptyList()
                    }
                }
            }
            else -> {
                logger.error(message = "Unknown parent declaration", symbol = parentNode)
                return emptyList()
            }
        }
    }

    private fun KSTypeAlias.getClassItems(resolver: KeepResolver): Collection<KeepClassItem> {
        val parentNode = parent
        when (parentNode) {
            is KSFile -> {
                val qualifiedName = resolver.getJvmName(parentNode)
                val name = name.asString() + "*"
                val types = emptyList<String>()
                val function = KeepClassItem.Function(name, types)
                val classItem = createClassItem(qualifiedName, field = null, constructor = null, function)
                return listOf(classItem)
            }
            else -> {
                logger.error(message = "Unknown parent declaration", symbol = parentNode)
                return emptyList()
            }
        }
    }

    //////
    //////
    //////

    private fun findClassItems(
        resolver: KeepResolver,
        classDeclaration: KSClassDeclaration,
        field: KeepClassItem.Field?,
        constructor: KeepClassItem.Constructor?,
        function: KeepClassItem.Function?
    ): Collection<KeepClassItem> {
        val allClassItems = mutableMapOf<String, KeepClassItem>()

        val currentClassItems = getCurrentClassItems(resolver, classDeclaration, field, constructor, function)
        currentClassItems.forEach { allClassItems.putOrMerge(it) }

        val parentsClassItems = getParentsClassItems(resolver, classDeclaration)
        parentsClassItems.forEach { allClassItems.putOrMerge(it) }

        return allClassItems.values
    }

    private fun getParentsClassItems(resolver: KeepResolver, classDeclaration: KSClassDeclaration): Collection<KeepClassItem> {
        val allClassItems = mutableMapOf<String, KeepClassItem>()

        var currentClassDeclaration = classDeclaration
        while (true) {

            val parentNode = currentClassDeclaration.parent
            if (parentNode !is KSClassDeclaration) {
                break
            }

            val classItems = getCurrentClassItems(resolver, parentNode, field = null, constructor = null, function = null)
            classItems.forEach { allClassItems.putOrMerge(it) }

            currentClassDeclaration = parentNode
        }

        return allClassItems.values
    }

    private fun getCurrentClassItems(
        resolver: KeepResolver,
        classDeclaration: KSClassDeclaration,
        field: KeepClassItem.Field?,
        constructor: KeepClassItem.Constructor?,
        function: KeepClassItem.Function?
    ): Collection<KeepClassItem> {
        when (classDeclaration.classKind) {
            ClassKind.ENUM_ENTRY -> {
                val qualifiedName = resolver.getJvmName(classDeclaration.parent as KSClassDeclaration)
                val classItem = createClassItem(qualifiedName, field, constructor, function)
                return listOf(classItem)
            }
            ClassKind.OBJECT -> {
                if (classDeclaration.isCompanionObject) {
                    val qualifiedName = resolver.getJvmName(classDeclaration)
                    val classItem = createClassItem(qualifiedName, field, constructor, function)

                    val parentFieldName = qualifiedName.substringAfterLast(delimiter = "$")
                    val parentField = KeepClassItem.Field(name = parentFieldName, type = qualifiedName)

                    val parentQualifiedName = resolver.getJvmName(classDeclaration.parent as KSClassDeclaration)
                    val parentClassItem = createClassItem(parentQualifiedName, parentField, constructor = null, function = null)

                    return listOf(parentClassItem, classItem)
                } else {
                    val qualifiedName = resolver.getJvmName(classDeclaration)
                    val instanceField = KeepClassItem.Field(name = "INSTANCE", type = qualifiedName)
                    val fields = setFrom(field, instanceField)
                    val classItem = createClassItem(qualifiedName, fields, constructor, function)
                    return listOf(classItem)
                }
            }
            else -> {
                val qualifiedName = resolver.getJvmName(classDeclaration)
                val classItem = createClassItem(qualifiedName, field, constructor, function)
                return listOf(classItem)
            }
        }
    }

    //////
    //////
    //////

    private fun MutableMap<String, KeepClassItem>.putOrMerge(classItem: KeepClassItem) {
        putOrMerge(classItem.name, classItem) { oldValue, newValue -> oldValue.mergeWith(newValue) }
    }

    private fun <K, V> MutableMap<K, V>.putOrMerge(key: K, value: V, block: (oldValue: V, newValue: V) -> V) {
        val newValue = value
        val oldValue = get(key)
        if (oldValue == null) {
            put(key, newValue)
        } else {
            val mergedValue = block(oldValue, newValue)
            put(key, mergedValue)
        }
    }

    private fun KeepClassItem.mergeWith(classItem: KeepClassItem): KeepClassItem {
        check(classItem.name == name) { classItem.name + " != " + name }
        return KeepClassItem(
            name = name,
            fields = fields.mergeWith(classItem.fields),
            constructors = constructors.mergeWith(classItem.constructors),
            functions = functions.mergeWith(classItem.functions)
        )
    }

    private fun <T> Collection<T>.mergeWith(elements: Collection<T>): Collection<T> {
        val mergedElements = toMutableSet()
        mergedElements.addAll(elements)
        return mergedElements
    }

    //////
    //////
    //////

    private fun createClassItem(
        name: String,
        field: KeepClassItem.Field?,
        constructor: KeepClassItem.Constructor?,
        function: KeepClassItem.Function?
    ): KeepClassItem {
        val fields = setFrom(field)
        return createClassItem(name, fields, constructor, function)
    }

    private fun createClassItem(
        name: String,
        fields: Set<KeepClassItem.Field>,
        constructor: KeepClassItem.Constructor?,
        function: KeepClassItem.Function?
    ): KeepClassItem {
        val constructors = setFrom(constructor)
        val functions = setFrom(function)
        return KeepClassItem(name, fields, constructors, functions)
    }

    //////
    //////
    //////

    private fun <T> setFrom(value: T?): Set<T> {
        if (value == null) {
            return emptySet()
        }

        return setOf(value)
    }

    private fun <T> setFrom(vararg values: T?): Set<T> {
        val filteredValues = values.mapNotNull { it }
        if (filteredValues.size == 0) {
            return emptySet()
        }

        return filteredValues.toSet()
    }

}