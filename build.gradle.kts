import org.apache.tools.ant.taskdefs.condition.Os

plugins {
    id("kotlin-multiplatform")
    id("kotlinx-serialization")
    id("com.jfrog.artifactory") version ("4.9.8")
    id("com.jfrog.bintray") version ("1.8.3")
}
apply(from = "https://gist.githubusercontent.com/IgorKey/1a3577ba3cdafe7dc2c52bcaebcfb00d/raw/fedf6b3200297f244703997bd24a733bd3e056a8/publish.gradle")
repositories {
    mavenCentral()
    jcenter()
}

kotlin {
    targets {
        if (!Os.isFamily(Os.FAMILY_MAC)) {
            mingwX64("windowsX64")
            linuxX64("linuxX64")
            jvm()
        } else {
            macosX64("macosX64")
            jvm()
        }

    }

    sourceSets {
        jvm().compilations["main"].defaultSourceSet {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime:$serializationRuntimeVersion")
            }
        }

        val commonMain by getting
        commonMain.apply {
            dependencies {
                implementation("org.jetbrains.kotlin:kotlin-stdlib-common")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-common:$serializationRuntimeVersion")
            }
        }


        val commonNativeSs by creating {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-runtime-native:$serializationRuntimeVersion")
            }
        }
        if (!Os.isFamily(Os.FAMILY_MAC)) {
            @Suppress("UNUSED_VARIABLE") val windowsX64Main by getting { dependsOn(commonNativeSs) }
            @Suppress("UNUSED_VARIABLE") val linuxX64Main by getting { dependsOn(commonNativeSs) }
        } else {
            @Suppress("UNUSED_VARIABLE") val macosX64Main by getting { dependsOn(commonNativeSs) }
        }
    }
}
