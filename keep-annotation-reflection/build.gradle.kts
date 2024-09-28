import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "dev.g000sha256"
version = "0.0.2"

plugins {
    alias(catalog.plugins.android.library)
    alias(catalog.plugins.g000sha256.sonatypeMavenCentral)
    alias(catalog.plugins.jetbrains.binaryCompatibilityValidator)
    alias(catalog.plugins.jetbrains.kotlin.android)
    id("org.gradle.maven-publish")
    id("org.gradle.signing")
}

android {
    buildToolsVersion = "34.0.0"
    compileSdk = 34
    namespace = "dev.g000sha256.keep.annotation.reflection"

    buildTypes {
        release { consumerProguardFile("proguard.pro") }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    defaultConfig { minSdk = 21 }

    publishing {
        singleVariant("release") {
            withJavadocJar()
            withSourcesJar()
        }
    }
}

kotlin {
    explicitApi()

    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
        moduleName = "dev.g000sha256.keep.annotation.reflection"
    }
}

dependencies {
    implementation(catalog.jetbrains.annotations)
    implementation(catalog.jetbrains.kotlin)
}

publishing {
    publications {
        afterEvaluate {
            register<MavenPublication>("release") {
                val component = components["release"]
                from(component)

                pom {
                    name = "Keep Annotation Reflection"
                    description = "The annotation and the R8 rules for keeping declarations used by reflection"
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