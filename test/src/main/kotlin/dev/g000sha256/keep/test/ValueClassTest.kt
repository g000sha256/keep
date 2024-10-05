package dev.g000sha256.keep.test

import dev.g000sha256.keep.annotation.api.KeepAPI

@JvmInline
public value class ValueClassTest public constructor(private val value: Int) {

    @KeepAPI
    public fun methodWithAnnotation() {
    }

    public fun methodWithoutAnnotation() {
    }

}