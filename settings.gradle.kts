include(":keep-annotation-api")
include(":keep-annotation-reflection")
include(":keep-compiler")
include(":keep-plugin")
include(":test")

pluginManagement {
    repositories {
        mavenLocal()
        google()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    @Suppress("UnstableApiUsage")
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }

    versionCatalogs {
        register("catalog") {
            val file = files("catalog.toml")
            from(file)
        }
    }
}