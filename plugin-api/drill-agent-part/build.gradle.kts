plugins {
    kotlin("multiplatform")
    `maven-publish`
}

val drillLoggerApiVersion: String by extra
val kxSerializationVersion: String by extra
val kxCoroutinesVersion: String by extra

kotlin {
    linuxX64()
    macosX64()
    mingwX64()
    jvm {
        val main by compilations
        main.defaultSourceSet {
            dependencies {
                compileOnly(kotlin("stdlib"))
            }
        }
    }

    sourceSets.commonMain {
        dependencies {
            api("com.epam.drill.logger:logger-api:$drillLoggerApiVersion")
        }
    }
}
