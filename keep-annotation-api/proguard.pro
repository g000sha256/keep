-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keep @dev.g000sha256.keep.annotation.api.KeepAPI class *

-keepclassmembers class * {

    @dev.g000sha256.keep.annotation.api.KeepAPI *;

}

-keepclassmembers @dev.g000sha256.keep.annotation.api.KeepAPI class * {

    public static final **$Companion Companion;

}

-keepclassmembers @dev.g000sha256.keep.annotation.api.KeepAPI class * {

    public static final ** INSTANCE;

}