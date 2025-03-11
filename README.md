# Keep

[![Maven Central](https://img.shields.io/maven-central/v/dev.g000sha256/keep-plugin?label=Maven%20Central&labelColor=171B35&color=E38E33)](https://central.sonatype.com/artifact/dev.g000sha256/keep-plugin)

The Gradle plugin simplifies the process of preserving necessary declarations from obfuscation when using the R8 tool.
It provides the `KeepApi` and `KeepReflection` annotations, along with corresponding R8 rules that are
automatically applied to your project based on the annotations used.

## Initialization

### Add plugin repository

```kotlin
repositories {
    mavenCentral()
}
```

### Apply plugin

```kotlin
plugins {
    id("dev.g000sha256.keep") version "1.0.0"
}
```

## Usage

The `KeepApi` and `KeepReflection` annotations are used similarly but serve different purposes.

- Use the `KeepReflection` annotation to preserve declarations accessed via reflection.
- Use the `KeepApi` annotation to preserve API declarations in library modules.

> [!TIP]
> You may add annotations only for the necessary declarations. Parent declarations will be added automatically.

### Classes

```kotlin
@KeepApi
class TestClass
```

```kotlin
class TestParentClass {

    @KeepApi
    class TestInnerClass

}
```

### Objects

```kotlin
@KeepApi
object TestObject
```

### Companion objects

```kotlin
class TestClass {

    @KeepApi
    companion object

}
```

```kotlin
class TestClass {

    @KeepApi
    companion object TestCompanionObject

}
```

### Getters

```kotlin
class TestClass {

    @get:KeepApi
    val testValue: Int = 0

}
```

### Setters

```kotlin
class TestClass {

    @set:KeepApi
    var testValue: Int = 0

}
```

### Constructors

```kotlin
class TestClass @KeepApi constructor(val testParameter: Int)
```

### Constructor parameters

```kotlin
class TestClass(@get:KeepApi val testParameter: Int)
```

### Methods

```kotlin
class TestClass {

    @KeepApi
    fun testMethod() {
    }

}
```

## Roadmap

- Constructor parameter types
- Field types
- Method parameter types
- `@JvmStatic` and `@JvmField` support