import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    kotlin("jvm")
    id("kotlinx-serialization")
    `maven-publish`
    idea
}

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()

}

dependencies{
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.2")
    implementation("org.jetbrains.xodus:xodus-entity-store:1.3.91")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationRuntimeVersion")
    testImplementation(kotlin("test-junit"))
}

tasks {

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
        kotlinOptions.freeCompilerArgs += "-Xuse-experimental=io.ktor.locations.KtorExperimentalLocationsAPI"
        kotlinOptions.freeCompilerArgs += "-Xuse-experimental=io.ktor.util.KtorExperimentalAPI"
        kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.Experimental"
        kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlinx.serialization.ImplicitReflectionSerializer"
    }

}