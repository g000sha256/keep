-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes RuntimeInvisibleAnnotations

-keep class dev.g000sha256.keep.annotation.reflection.KeepReflection

-keep @dev.g000sha256.keep.annotation.reflection.KeepReflection class *

-keepclassmembers class * {

    @dev.g000sha256.keep.annotation.reflection.KeepReflection *;

}

-keepclassmembers @dev.g000sha256.keep.annotation.reflection.KeepReflection class * {

    public static final **$Companion Companion;

}

-keepclassmembers @dev.g000sha256.keep.annotation.reflection.KeepReflection class * {

    public static final ** INSTANCE;

}