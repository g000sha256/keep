import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "dev.g000sha256"
version = "1.0.0"

plugins {
    alias(catalog.plugins.g000sha256.sonatypeMavenCentral)
    alias(catalog.plugins.jetBrains.binaryCompatibilityValidator)
    alias(catalog.plugins.jetBrains.dokka)
    alias(catalog.plugins.jetBrains.kotlin.jvm)
    id("org.gradle.maven-publish")
    id("org.gradle.signing")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

kotlin {
    explicitApi()

    compilerOptions {
        jvmTarget = JvmTarget.JVM_1_8
        moduleName = "dev.g000sha256.keep.annotation.api"
    }
}

dependencies {
    implementation(catalog.jetBrains.annotations)
    implementation(catalog.jetBrains.kotlin)
}

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier = "sources"

    val sourceSet = sourceSets.main.get()
    from(sourceSet.allSource)
}

val dokkaJavadoc by tasks.getting(DokkaTask::class)

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier = "javadoc"

    dependsOn(dokkaJavadoc)
    from(dokkaJavadoc.outputDirectory)
}

publishing {
    publications {
        register<MavenPublication>("release") {
            val component = components["kotlin"]
            from(component)

            artifact(javadocJar)
            artifact(sourcesJar)

            pom {
                name = "Keep API Annotation"
                description = "An annotation to prevent obfuscation of API declarations in library modules"
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