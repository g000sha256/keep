import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "dev.g000sha256"
version = "0.0.2"

plugins {
    alias(catalog.plugins.g000sha256.sonatypeMavenCentral)
    alias(catalog.plugins.google.ksp)
    alias(catalog.plugins.jetbrains.binaryCompatibilityValidator)
    alias(catalog.plugins.jetbrains.kotlin.jvm)
    alias(catalog.plugins.jetbrains.kotlin.kapt)
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
        moduleName = "dev.g000sha256.keep.compiler"
    }
}

dependencies {
    implementation(catalog.jetbrains.annotations)
    implementation(catalog.jetbrains.kotlin)

    implementation(catalog.google.ksp)

    kapt(catalog.google.autoService)
    implementation(catalog.google.autoServiceAnnotations)

//    val projectApi = project(":keep-annotation-api")
//    implementation(projectApi) // TODO Change type from android to jvm

//    val projectReflection = project(":keep-annotation-reflection")
//    implementation(projectReflection) // TODO Change type from android to jvm
}

publishing {
    publications {
        register<MavenPublication>("release") {
            val component = components["kotlin"]
            from(component)

            // TODO Add pom
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