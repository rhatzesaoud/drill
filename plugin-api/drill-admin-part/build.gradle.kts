plugins {
    id("kotlin-multiplatform")
    id("kotlinx-serialization")
}
apply(from = "https://raw.githubusercontent.com/Drill4J/build-scripts/master/publish.gradle")

kotlin {
    targets {
        jvm()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializationRuntimeVersion")
                implementation(project(":common"))
                implementation("com.epam.drill:kodux:0.1.1")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationRuntimeVersion")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
                implementation(project(":common"))
                implementation("com.epam.drill:kodux-jvm:0.1.0")
            }
        }
    }
}
tasks.build {
    dependsOn("publishToMavenLocal")
}
