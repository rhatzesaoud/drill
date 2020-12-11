plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `maven-publish`
}

kotlin {
    linuxX64()
    macosX64()
    mingwX64()
    jvm()

    sourceSets.commonMain {
        dependencies {
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common")
        }
    }

    targets.matching { it.platformType == org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.native }.all {
        val main by compilations
        main.defaultSourceSet {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native")
            }
        }
    }

    jvm {
        val main by compilations
        main.defaultSourceSet {
            dependencies {
                compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime")
            }
        }
    }
}
