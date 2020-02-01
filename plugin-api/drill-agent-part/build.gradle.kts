plugins {
    id("kotlin-multiplatform")
    id("kotlinx-serialization")
}
apply(from = "https://raw.githubusercontent.com/Drill4J/build-scripts/master/publish.gradle")

kotlin {
    targets {
        jvm()
        mingwX64("windowsX64")
        linuxX64("linuxX64")
        macosX64("macosX64")
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializationRuntimeVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation(project(":common"))
            }
        }
        named("jvmMain") {
            dependencies {
                implementation(project(":common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationRuntimeVersion")
            }
        }
        val nativeMain by creating {
            dependencies {
                implementation(project(":common"))
                implementation("com.epam.drill:jvmapi-native:$drillJvmApiLibVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesNativeVersion")
            }
        }
        @Suppress("UNUSED_VARIABLE") val windowsX64Main by getting { dependsOn(nativeMain) }
        @Suppress("UNUSED_VARIABLE") val linuxX64Main by getting { dependsOn(nativeMain) }
        @Suppress("UNUSED_VARIABLE") val macosX64Main by getting { dependsOn(nativeMain) }

    }
}
tasks.build {
    dependsOn("publishToMavenLocal")
}