plugins {
    kotlin("multiplatform")
    `maven-publish`
}

kotlin {
    jvm()

    sourceSets.commonMain {
        dependencies {
            compileOnly(kotlin("stdlib"))
        }
    }
}
