package dev.g000sha256.keep.test

import dev.g000sha256.keep.annotation.api.KeepAPI

public enum class EnumClassTest {

    @KeepAPI
    VALUE_WITH_ANNOTATION,

    VALUE_WITHOUT_ANNOTATION;

    @KeepAPI
    public fun methodWithAnnotation() {
    }

    public fun methodWithoutAnnotation() {
    }

}