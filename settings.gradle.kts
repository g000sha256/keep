include(":keep-annotation-api")
include(":keep-annotation-reflection")
include(":keep-compiler")
include(":keep-plugin")
//include(":test")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
//        mavenLocal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        google()
        mavenCentral()
//        mavenLocal()
    }

    versionCatalogs {
        register("catalog") {
            val file = files("catalog.toml")
            from(file)
        }
    }
}