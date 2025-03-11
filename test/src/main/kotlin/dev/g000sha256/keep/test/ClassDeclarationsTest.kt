package dev.g000sha256.keep.test

import dev.g000sha256.keep.annotation.api.KeepApi
import kotlin.time.Duration

public class ClassDeclarationsTest @KeepApi public constructor(
    @get:KeepApi
    @set:KeepApi
    public var param: Duration
) {

    @KeepApi
    public val property: Duration = Duration.ZERO

    @get:KeepApi
    public val getter: Duration = Duration.ZERO

    public val Duration.getter: Duration
        @KeepApi
        get() = Duration.ZERO

    @set:KeepApi
    public var setter: Duration = Duration.ZERO

    public var Duration.setter: Duration
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