@file:JvmName("Qwerty")

package dev.g000sha256.keep.test

import dev.g000sha256.keep.annotation.api.KeepAPI

public class Test1 public constructor() {

    public object Test2 {

        public class Test3 public constructor() {

            public companion object Test4 {

                public abstract class Test5 public constructor() {

                    @KeepAPI
                    public abstract fun method1()

                    @KeepAPI
                    public abstract fun method2()

                }

            }

        }

    }

}

@JvmInline
public value class ValueClassTest public constructor(private val value: Int) {

    @KeepAPI
    public fun method() {
    }

}

@KeepAPI
public typealias TypealiasClassTest = Test1

public enum class EnumClassTest {

    ONE,

    TWO;

    @KeepAPI
    public fun method() {
    }

}

@KeepAPI
public fun method() {
}