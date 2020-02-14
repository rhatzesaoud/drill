plugins {
    id("kotlin-multiplatform")
    id("kotlinx-serialization")
    id("com.epam.drill.cross-compilation")
}

repositories {
    mavenCentral()
    jcenter()
}

kotlin {

    linuxX64()
    macosX64()
    mingwX64()
    crossCompilation {

        common {
            defaultSourceSet {
                dependencies {
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$serializationRuntimeVersion")
                }
            }
        }
    }
    jvm()


    sourceSets {
        jvm {
            compilations["main"].defaultSourceSet {
                dependencies {
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationRuntimeVersion")
                }
                compilations["test"].defaultSourceSet {
                    dependencies {
                        implementation(kotlin("test-junit"))
                    }
                }
            }
        }
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializationRuntimeVersion")
            }
        }

        commonTest {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-test-common")
                implementation("org.jetbrains.kotlin:kotlin-test-annotations-common")
            }
        }

    }
}

tasks.withType<AbstractTestTask> {
    testLogging.showStandardStreams = true
}
tasks.withType<org.jetbrains.kotlin.gradle.dsl.KotlinCompile<org.jetbrains.kotlin.gradle.dsl.KotlinCommonOptions>> {
    kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlinx.serialization.ImplicitReflectionSerializer"
}
tasks.build {
    dependsOn("publishToMavenLocal")
}
