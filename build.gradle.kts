plugins {
    kotlin("multiplatform") apply false
    `maven-publish`
}

val scriptUrl: String by extra

val kotlinVersion: String by extra

val drillLoggerApiVersion: String by extra

val kxSerializationVersion: String by extra
val kxCoroutinesVersion: String by extra
val koduxVersion: String by extra

val constraints = listOf(
    "com.epam.drill.logger:logger-api:$drillLoggerApiVersion",
    "org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kxSerializationVersion",
    "org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kxSerializationVersion",
    "org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kxSerializationVersion",
    "org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$kxSerializationVersion",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kxCoroutinesVersion",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$kxCoroutinesVersion",
    "com.epam.drill:kodux:$koduxVersion",
    "com.epam.drill:kodux-jvm:$koduxVersion"
).map(dependencies.constraints::create)

allprojects {
    apply(from = rootProject.uri("$scriptUrl/git-version.gradle.kts"))
}

subprojects {
    repositories {
        mavenLocal()
        apply(from = "$scriptUrl/maven-repo.gradle.kts")
        jcenter()
    }

    configurations.all {
        dependencyConstraints += constraints
    }
}
