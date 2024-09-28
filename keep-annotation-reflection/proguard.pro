-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keepattributes RuntimeInvisibleAnnotations

-keep class dev.g000sha256.keep.annotation.reflection.KeepReflection

-keepclassmembers class * {

    @dev.g000sha256.keep.annotation.reflection.KeepReflection *;

}