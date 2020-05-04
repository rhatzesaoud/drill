import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    id("com.epam.drill.version.plugin")
    `maven-publish`
}

val scriptUrl: String by extra

val kotlinVersion: String by extra
val kxSerializationVersion: String by extra
val kxCoroutinesVersion: String by extra
val koduxVersion: String by extra

val constraints = listOf(
    "org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kxSerializationVersion",
    "org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kxSerializationVersion",
    "org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kxSerializationVersion",
    "org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$kxSerializationVersion",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core:$kxCoroutinesVersion",
    "org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$kxCoroutinesVersion",
    "com.epam.drill:kodux:$koduxVersion",
    "com.epam.drill:kodux-jvm:$koduxVersion"
).map(dependencies.constraints::create)

subprojects {
    apply(plugin = "com.epam.drill.version.plugin")

    repositories {
        mavenLocal()
        apply(from = "$scriptUrl/maven-repo.gradle.kts")
        jcenter()
    }

    configurations.all {
        dependencyConstraints += constraints
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.allWarningsAsErrors = true
    }
}
