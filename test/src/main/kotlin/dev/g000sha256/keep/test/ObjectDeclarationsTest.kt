package dev.g000sha256.keep.test

import dev.g000sha256.keep.annotation.api.KeepApi
import kotlin.time.Duration

public object ObjectDeclarationsTest {

    @KeepApi
    public val property: Duration = Duration.ZERO

    public val getter: Duration
        @KeepApi
        get() = Duration.ZERO

    public val Duration.getter: Duration
        @KeepApi
        get() = Duration.ZERO

    public var setter: Duration
        get() = Duration.ZERO
        @KeepApi
        set(value) {
        }

    public var String.setter: Duration
        get() = Duration.ZERO
        @KeepApi
        set(value) {
        }

    @KeepApi
    public fun function(): Duration {
        return Duration.ZERO
    }

    @KeepApi
    public fun Duration.extensionFunction() {
    }

    @KeepApi
    public fun parametersFunction(duration: Duration) {
    }

    @KeepApi
    public suspend fun suspendFunction(): Duration {
        return Duration.ZERO
    }

}