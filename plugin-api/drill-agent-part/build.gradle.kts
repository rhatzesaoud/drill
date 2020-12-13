plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.epam.drill.cross-compilation")
    `maven-publish`
}

val drillLoggerApiVersion: String by extra
val kxSerializationVersion: String by extra
val kxCoroutinesVersion: String by extra

kotlin {
    linuxX64()
    macosX64()
    mingwX64()
    jvm()

    sourceSets.commonMain {
        dependencies {
            api(project(":common"))
            api("com.epam.drill.logger:logger-api:$drillLoggerApiVersion")

            implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$kxSerializationVersion")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kxCoroutinesVersion")
        }
    }
    sourceSets.commonTest {
        dependencies {
            implementation(kotlin("test-common"))
            implementation(kotlin("test-annotations-common"))
        }
    }

    crossCompilation {
        common {
            defaultSourceSet {
                dependsOn(sourceSets.commonMain.get())
                dependencies {
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$kxCoroutinesVersion")
                }
            }
        }
    }

    jvm {
        val main by compilations
        main.defaultSourceSet {
            dependencies {
                compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$kxSerializationVersion")
            }
        }
    }
}
