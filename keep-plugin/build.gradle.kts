import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "dev.g000sha256"
version = "1.0.0"

plugins {
    alias(catalog.plugins.g000sha256.sonatypeMavenCentral)
    alias(catalog.plugins.gmazzo.buildConfig)
    alias(catalog.plugins.jetBrains.binaryCompatibilityValidator)
    alias(catalog.plugins.jetBrains.kotlin.jvm)
    id("org.gradle.java-gradle-plugin")
    id("org.gradle.maven-publish")
    id("org.gradle.signing")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11

    withJavadocJar()
    withSourcesJar()
}

kotlin {
    explicitApi()

    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
        moduleName = "dev.g000sha256.keep.plugin"
    }
}

buildConfig {
    val projectAnnotationApi = project(":keep-annotation-api")
    buildConfigField("VERSION_ANNOTATION_API", projectAnnotationApi.version as String)

    val projectAnnotationReflection = project(":keep-annotation-reflection")
    buildConfigField("VERSION_ANNOTATION_REFLECTION", projectAnnotationReflection.version as String)

    val projectCompiler = project(":keep-compiler")
    buildConfigField("VERSION_COMPILER", projectCompiler.version as String)
}

dependencies {
    implementation(catalog.jetBrains.annotations)
    implementation(catalog.jetBrains.kotlin)

    implementation(catalog.android.gradle)
    implementation(catalog.android.gradleApi)
    implementation(catalog.android.gradleBuilder)
    implementation(catalog.android.gradleBuilderModel)
    implementation(catalog.google.kspPlugin)
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                when (this@withType.name) {
                    "releasePluginMarkerMaven" -> {
                        name = "Keep Gradle Plugin Marker"
                        description = "A marker that allows you to add the Keep Gradle Plugin to your project using its ID"
                    }
                    "pluginMaven" -> {
                        name = "Keep Gradle Plugin"
                        description = "A Gradle plugin that adds dependencies, configures obfuscation rules, and registers necessary tasks"
                    }
                    else -> error("Unknown publication")
                }

                url = "https://github.com/g000sha256/keep"
                inceptionYear = "2025"

                licenses {
                    license {
                        name = "Apache License 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }

                developers {
                    developer {
                        id = "g000sha256"
                        name = "Georgii Ippolitov"
                        email = "detmmpmznb@g000sha256.dev"
                        url = "https://github.com/g000sha256"
                    }
                }

                scm {
                    connection = "scm:git:git://github.com/g000sha256/keep.git"
                    developerConnection = "scm:git:ssh://github.com:g000sha256/keep.git"
                    url = "https://github.com/g000sha256/keep/tree/master"
                }

                issueManagement {
                    system = "GitHub Issues"
                    url = "https://github.com/g000sha256/keep/issues"
                }
            }
        }
    }
}

gradlePlugin {
    plugins {
        register("release") {
            id = "dev.g000sha256.keep"
            implementationClass = "dev.g000sha256.keep.KeepPlugin"
        }
    }
}

sonatypeMavenCentralRepository {
    credentials {
        username = getProperty("SonatypeMavenCentral.Username") ?: getEnvironment("SONATYPE_USERNAME")
        password = getProperty("SonatypeMavenCentral.Password") ?: getEnvironment("SONATYPE_PASSWORD")
    }
}

signing {
    val key = getProperty("Signing.Key") ?: getEnvironment("SIGNING_KEY")
    val password = getProperty("Signing.Password") ?: getEnvironment("SIGNING_PASSWORD")
    useInMemoryPgpKeys(key, password)

    sign(publishing.publications)
}

private fun getProperty(key: String): String? {
    return properties[key] as String?
}

private fun getEnvironment(key: String): String? {
    return System.getenv(key)
}