plugins {
    id("kotlin-multiplatform")
    id("kotlinx-serialization")
    id("com.jfrog.artifactory") version ("4.9.8")
    id("com.jfrog.bintray") version ("1.8.3")
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
                implementation("com.epam.drill:common:$drillCommonLibVersion")
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationRuntimeVersion")
                implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
                implementation("com.epam.drill:common-jvm:$drillCommonLibVersion")
            }
        }
    }
}
tasks.build {
    dependsOn("publishToMavenLocal")
}
