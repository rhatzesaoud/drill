import org.jetbrains.kotlin.konan.target.*

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
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common")
        }
    }

    crossCompilation {
        common {
            defaultSourceSet {
                dependencies {
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native")
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
                implementation(kotlin("test-junit"))
            }
        }
    }
}

tasks.withType<AbstractTestTask> {
    testLogging.showStandardStreams = true
}

tasks.register("targetTest") {
    group = "verification"
    dependsOn("${HostManager.host.presetName}Test")
    dependsOn("jvmTest")
}
