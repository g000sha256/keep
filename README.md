# Keep

[![Maven Central](https://img.shields.io/maven-central/v/dev.g000sha256/keep-plugin?label=Maven%20Central&labelColor=171B35&color=E38E33)](https://central.sonatype.com/artifact/dev.g000sha256/keep-plugin)

The Gradle plugin simplifies the process of preserving necessary declarations from obfuscation when using the R8 tool.
It provides the `KeepAPI` and `KeepReflection` annotations, along with corresponding R8 rules that are
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
    id("dev.g000sha256.keep") version "0.0.1"
}
```

## Usage

The `KeepAPI` and `KeepReflection` annotations are used similarly but serve different purposes.

- Use the `KeepReflection` annotation to preserve declarations accessed via reflection.
- Use the `KeepAPI` annotation to preserve API declarations in library modules.

> [!IMPORTANT]
> Annotating methods and fields does not automatically preserve the class name.
> Don't forget to add the annotation to the class if necessary.

### Class name only

```kotlin
@KeepAPI
class TestClass
```

```kotlin
@KeepAPI
class TestParentClass {

    @KeepAPI
    class TestInnerClass

}
```

### Getters and Setters

```kotlin
@KeepAPI
class TestClass {

    @get:KeepAPI
    val testValue: Int = 0

}
```

```kotlin
@KeepAPI
class TestClass {

    @set:KeepAPI
    var testValue: Int = 0

}
```

```kotlin
@KeepAPI
class TestClass {

    @get:KeepAPI
    @set:KeepAPI
    var testValue: Int = 0

}
```

### Constructors

```kotlin
@KeepAPI
class TestClass @KeepAPI constructor(val testValue: Int)
```

```kotlin
@KeepAPI
class TestClass @KeepAPI constructor(@get:KeepAPI val testValue: Int)
```

### Methods

```kotlin
@KeepAPI
class TestClass {

    @KeepAPI
    fun testMethod() {
    }

}
```