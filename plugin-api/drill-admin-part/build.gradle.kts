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
            implementation("com.epam.drill:kodux") { isTransitive = false }
        }
    }

    jvm {
        val main by compilations
        main.defaultSourceSet {
            dependencies {
                api(project(":common"))
                compileOnly("org.jetbrains.kotlinx:kotlinx-serialization-runtime")
                compileOnly("com.epam.drill:kodux-jvm")
            }
        }
    }
}
