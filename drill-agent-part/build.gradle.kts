plugins {
    id("kotlin-multiplatform")
    id("kotlinx-serialization")
}
apply(from = rootProject.file("gradle/publish.gradle"))
kotlin {
    targets {
        jvm()
        mingwX64("windowsX64")
        linuxX64("linuxX64")
        macosX64("macosX64")
    }

    sourceSets {
        val commonMain by getting{
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationRuntimeVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation("com.epam.drill:drill-common:$version")
            }
        }
        named("jvmMain") {
            dependencies {
                implementation("com.epam.drill:drill-common-jvm:$version")
            }
        }
        val nativeMain by creating {
            dependencies {
                implementation("com.epam.drill:drill-jvmapi-native:$version")
                implementation("com.epam.drill:drill-common-native:$version")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesVersion")
            }
        }
        @Suppress("UNUSED_VARIABLE") val windowsX64Main by getting { dependsOn(nativeMain) }
        @Suppress("UNUSED_VARIABLE") val linuxX64Main by getting { dependsOn(nativeMain) }
        @Suppress("UNUSED_VARIABLE") val macosX64Main by getting { dependsOn(nativeMain) }

    }
}