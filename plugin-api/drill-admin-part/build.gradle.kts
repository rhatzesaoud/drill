plugins {
    kotlin("multiplatform")
    kotlin("plugin.serialization")
    `maven-publish`
}

kotlin {
    sourceSets.commonMain {
        dependencies {
            api(project(":common"))
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common")
        }
    }

    jvm {
        val main by compilations
        main.defaultSourceSet {
            dependencies {
                api(project(":common"))
                compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime")
            }
        }
    }
}
