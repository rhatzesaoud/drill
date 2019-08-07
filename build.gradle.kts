plugins {
    id("kotlin-multiplatform")
    id("kotlinx-serialization")
}
apply(from = rootProject.file("gradle/publish.gradle"))

repositories {
    mavenCentral()
    jcenter()
}

kotlin {
    targets {
        mingwX64("windowsX64")
        linuxX64("linuxX64")
        macosX64("macosX64")
        jvm()
    }

    sourceSets {
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationRuntimeVersion")
            }
        }

        val commonMain by getting
        commonMain.apply {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializationRuntimeVersion")
            }
        }


        val commonNativeSs by creating {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$serializationRuntimeVersion")
            }
        }
        @Suppress("UNUSED_VARIABLE") val windowsX64Main by getting { dependsOn(commonNativeSs) }
        @Suppress("UNUSED_VARIABLE") val linuxX64Main by getting { dependsOn(commonNativeSs) }
        @Suppress("UNUSED_VARIABLE") val macosX64Main by getting { dependsOn(commonNativeSs) }
    }
}
