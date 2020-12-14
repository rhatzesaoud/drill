plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `maven-publish`
}

val kxSerializationVersion: String by extra

kotlin {
    linuxX64()
    macosX64()
    mingwX64()
    jvm()

    sourceSets {
        all {
            languageSettings.useExperimentalAnnotation("kotlinx.serialization.InternalSerializationApi")
            languageSettings.useExperimentalAnnotation("kotlinx.serialization.ExperimentalSerializationApi")
        }

        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kxSerializationVersion")
            }
        }

        targets.matching { it.platformType == org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.native }.all {
            val main by compilations
            main.defaultSourceSet {
                dependencies {
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kxSerializationVersion")
                }
            }
        }

        jvm {
            val main by compilations
            main.defaultSourceSet {
                dependencies {
                    compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-core:$kxSerializationVersion")
                }
            }
        }
    }
}
