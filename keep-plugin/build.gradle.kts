import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "dev.g000sha256"
version = "0.0.2"

plugins {
    alias(catalog.plugins.g000sha256.sonatypeMavenCentral)
    alias(catalog.plugins.gmazzo.buildConfig)
    alias(catalog.plugins.jetbrains.binaryCompatibilityValidator)
    alias(catalog.plugins.jetbrains.kotlin.jvm)
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
        moduleName = "dev.g000sha256.keep"
    }
}

buildConfig {
    val projectApi = project(":keep-annotation-api")
    buildConfigField("VERSION_API", projectApi.version as String)

    val projectReflection = project(":keep-annotation-reflection")
    buildConfigField("VERSION_REFLECTION", projectReflection.version as String)
}

dependencies {
    implementation(catalog.jetbrains.annotations)
    implementation(catalog.jetbrains.kotlin)

    implementation(catalog.android.gradleApi)
}

publishing {
    publications {
        withType<MavenPublication> {
            val publicationName = name

            pom {
                when (publicationName) {
                    "releasePluginMarkerMaven" -> {
                        name = "Keep Plugin Marker"
                        description = "Allows adding the Keep Plugin to your project using its ID"
                    }
                    "pluginMaven" -> {
                        name = "Keep Plugin"
                        description = "The plugin for adding the annotations and the R8 rules for keeping declarations"
                    }
                    else -> throw IllegalStateException("Unknown publication")
                }

                url = "https://github.com/g000sha256/keep"
                inceptionYear = "2024"

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