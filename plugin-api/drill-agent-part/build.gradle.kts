plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    id("com.epam.drill.cross-compilation")
    `maven-publish`
}

kotlin {
    linuxX64()
    macosX64()
    mingwX64()
    jvm()

    sourceSets.commonMain {
        dependencies {
            api(project(":common"))
            api("com.epam.drill.logger:logger-api")

            implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common")
            implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
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
                    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native")
                }
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
        val test by compilations
        test.defaultSourceSet {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime")
                implementation(kotlin("test-junit"))
            }
        }
    }
}
