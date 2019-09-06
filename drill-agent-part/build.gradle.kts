plugins {
    id("kotlin-multiplatform")
    id("kotlinx-serialization")
    id("com.jfrog.artifactory") version ("4.9.8")
    id("com.jfrog.bintray") version ("1.8.3")
}
apply(from = "https://gist.githubusercontent.com/IgorKey/e7a0e07428b6e56283d08dbc605bb942/raw/0af997f4044c4d1e1667e9ee67b2ebdf736fabde/publish.gradle")

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
                implementation("com.epam.drill:common:$$drillCommonLibVersion")
            }
        }
        named("jvmMain") {
            dependencies {
                implementation("com.epam.drill:common-jvm:$$drillCommonLibVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationRuntimeVersion")
            }
        }
        val nativeMain by creating {
            dependencies {
                implementation("com.epam.drill:common-native:$$drillCommonLibVersion")
                implementation("com.epam.drill:jvmapi-native:$drillJvmApiLibVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesVersion")
            }
        }
        @Suppress("UNUSED_VARIABLE") val windowsX64Main by getting { dependsOn(nativeMain) }
        @Suppress("UNUSED_VARIABLE") val linuxX64Main by getting { dependsOn(nativeMain) }
        @Suppress("UNUSED_VARIABLE") val macosX64Main by getting { dependsOn(nativeMain) }

    }
}