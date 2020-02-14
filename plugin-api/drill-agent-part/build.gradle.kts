plugins {
    id("kotlin-multiplatform")
    id("kotlinx-serialization")
    id("com.epam.drill.cross-compilation")
}

kotlin {
    targets {
        jvm{
            compilations["main"].defaultSourceSet{
                dependencies {
                    implementation(project(":common"))
                    implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationRuntimeVersion")
                }
            }
        }
        linuxX64()
        macosX64()
        mingwX64()
        crossCompilation {

            common {
                defaultSourceSet {
                    dependsOn(sourceSets.named("commonMain").get())
                    dependencies {
                        implementation(project(":common"))
                        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-native:$coroutinesNativeVersion")
                    }
                }
            }
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
    }
}
tasks.build {
    dependsOn("publishToMavenLocal")
}