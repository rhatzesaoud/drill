import org.jetbrains.kotlin.gradle.plugin.*

plugins {
    id("kotlin-multiplatform")
    id("kotlinx-serialization")
}

repositories {
    mavenCentral()
    jcenter()
}

kotlin {
    targets {
        if (isDevMode)
            currentTarget {
                compilations["main"].apply {
                    defaultSourceSet {
                        kotlin.srcDir("./src/nativeCommonMain/kotlin")
                        applyDependencies()
                    }
                }
            }
        else {
            setOf(linuxX64(), macosX64(), mingwX64())
        }
        jvm()

    }

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

        if (!isDevMode) {
            val commonNativeMain = maybeCreate("nativeCommonMain")
            targets.filterIsInstance<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>().forEach {
                it.compilations.forEach { knCompilation ->
                    if (knCompilation.name == "main")
                        knCompilation.defaultSourceSet {
                            dependsOn(commonNativeMain)
                            applyDependencies()
                        }
                }
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

fun KotlinSourceSet.applyDependencies() {
    dependencies {
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$serializationRuntimeVersion")
    }
}