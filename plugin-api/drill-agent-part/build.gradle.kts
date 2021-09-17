plugins {
    kotlin("multiplatform")
    `maven-publish`
}

val drillLoggerApiVersion: String by extra

kotlin {
    targets {
        linuxX64()
        macosX64()
        mingwX64()
        jvm()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api("com.epam.drill.logger:logger-api:$drillLoggerApiVersion")
            }
        }
    }
}
