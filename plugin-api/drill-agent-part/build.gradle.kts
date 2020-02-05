plugins {
    id("kotlin-multiplatform")
    id("kotlinx-serialization")
}

kotlin {
    targets {
        jvm()
        if (isDevMode)
            currentTarget {
                compilations["main"].apply {
                    defaultSourceSet {
                        kotlin.srcDir("./src/nativeMain/kotlin")
                        applyDependencies()
                    }
                }
            }
        else {
            setOf(linuxX64(), macosX64(), mingwX64())
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializationRuntimeVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
                implementation(project(":common"))
            }
        }
        named("jvmMain") {
            dependencies {
                implementation(project(":common"))
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationRuntimeVersion")
            }
        }

        if (!isDevMode) {
            val commonNativeMain = maybeCreate("nativeMain")
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
tasks.build {
    dependsOn("publishToMavenLocal")
}

fun org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet.applyDependencies() {
    dependencies {
        implementation(project(":common"))
        implementation("com.epam.drill:jvmapi-native:$drillJvmApiLibVersion")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesNativeVersion")
    }
}