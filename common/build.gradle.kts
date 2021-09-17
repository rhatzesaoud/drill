plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `maven-publish`
}

val kxSerializationVersion: String by extra

kotlin {
    targets {
        linuxX64()
        macosX64()
        mingwX64()
        jvm()
    }
    sourceSets {
        val experimental = listOf(
            "kotlinx.serialization.InternalSerializationApi",
            "kotlinx.serialization.ExperimentalSerializationApi"
        )
        all { experimental.forEach(languageSettings::optIn) }

        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:$kxSerializationVersion")
            }
        }
    }
}
