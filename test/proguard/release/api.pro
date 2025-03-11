-keepattributes EnclosingMethod
-keepattributes InnerClasses

-keepclasseswithmembers class dev.g000sha256.keep.test.ClassDeclarationsTest {

    *** property;

    <init>(...);

    getParam*();

    setParam*(...);

    getGetter*();

    getGetter*(...);

    setSetter*(...);

    function*();

    extensionFunction*(...);

    parametersFunction*(...);

    suspendFunction*(...);

}

-keep class dev.g000sha256.keep.test.ClassTest

-keepclasseswithmembers class dev.g000sha256.keep.test.CompanionObjectDeclarationsTest {

    dev.g000sha256.keep.test.CompanionObjectDeclarationsTest$Companion Companion;

}

-keepclasseswithmembers class dev.g000sha256.keep.test.CompanionObjectDeclarationsTest$Companion {

    getProperty*();

    getGetter*();

    getGetter*(...);

    setSetter*(...);

    function*();

    extensionFunction*(...);

    parametersFunction*(...);

    suspendFunction*(...);

}

-keepclasseswithmembers class dev.g000sha256.keep.test.CompanionObjectTest {

    dev.g000sha256.keep.test.CompanionObjectTest$Companion Companion;

}

-keep class dev.g000sha256.keep.test.CompanionObjectTest$Companion

-keepclasseswithmembers class dev.g000sha256.keep.test.EnumClassDeclarationsTest {

    dev.g000sha256.keep.test.EnumClassDeclarationsTest VALUE;

}

-keep class dev.g000sha256.keep.test.EnumClassTest

-keepclasseswithmembers class dev.g000sha256.keep.test.FileDeclarationsTestKt {

    *** property;

    getGetter();

    getGetter*(...);

    setSetter*(...);

    function();

    extensionFunction*(...);

    parametersFunction*(...);

    suspendFunction(...);

}

-keep class dev.g000sha256.keep.test.FileTestKt

-keepclasseswithmembers class dev.g000sha256.keep.test.InterfaceDeclarationsTest {

    getGetter*();

    getGetter*(...);

    setSetter*(...);

    function*();

    extensionFunction*(...);

    parametersFunction*(...);

    suspendFunction*(...);

}

-keepclasseswithmembers class dev.g000sha256.keep.test.NamedCompanionObjectTest {

    dev.g000sha256.keep.test.NamedCompanionObjectTest$CustomCompanionObject CustomCompanionObject;

}

-keep class dev.g000sha256.keep.test.NamedCompanionObjectTest$CustomCompanionObject

-keep class dev.g000sha256.keep.test.CustomFileName

-keepclasseswithmembers class dev.g000sha256.keep.test.ObjectDeclarationsTest {

    dev.g000sha256.keep.test.ObjectDeclarationsTest INSTANCE;

    getProperty*();

    getGetter*();

    getGetter*(...);

    setSetter*(...);

    function*();

    extensionFunction*(...);

    parametersFunction*(...);

    suspendFunction*(...);

}

-keepclasseswithmembers class dev.g000sha256.keep.test.ObjectTest {

    dev.g000sha256.keep.test.ObjectTest INSTANCE;

}

-keepclasseswithmembers class dev.g000sha256.keep.test.TypealiasTestKt {

    TypealiasTest*();

}

