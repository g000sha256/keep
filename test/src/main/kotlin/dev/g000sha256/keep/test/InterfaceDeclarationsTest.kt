package dev.g000sha256.keep.test

import dev.g000sha256.keep.annotation.api.KeepApi
import kotlin.time.Duration

public interface InterfaceDeclarationsTest {

    @get:KeepApi
    public val getter: Duration

    @get:KeepApi
    public val Duration.getter: Duration

    @set:KeepApi
    public var setter: Duration

    @set:KeepApi
    public var String.setter: Duration

    @KeepApi
    public fun function(): Duration

    @KeepApi
    public fun Duration.extensionFunction()

    @KeepApi
    public fun parametersFunction(duration: Duration)

    @KeepApi
    public suspend fun suspendFunction(): Duration

}