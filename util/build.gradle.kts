import com.epam.drill.build.drillJvmApiLibVerison
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

plugins {
    id("kotlin-multiplatform")
}


kotlin {
    targets {
        mingwX64("win")
        linuxX64("linux")
        macosX64("mac")
    }

    sourceSets {
        val commonNativeMain: KotlinSourceSet by creating {
            dependencies {
                implementation("com.epam.drill:jvmapi-native:$drillJvmApiLibVerison")
                implementation("com.epam.drill:drill-agent-part-native:$version")
            }
        }
        @Suppress("UNUSED_VARIABLE") val winMain by getting { dependsOn(commonNativeMain) }
        @Suppress("UNUSED_VARIABLE") val linuxMain by getting { dependsOn(commonNativeMain) }
        @Suppress("UNUSED_VARIABLE") val macMain by getting { dependsOn(commonNativeMain) }


    }
}

tasks.withType<KotlinNativeCompile> {
    kotlinOptions.freeCompilerArgs += "-Xuse-experimental=kotlin.ExperimentalUnsignedTypes"
    kotlinOptions.freeCompilerArgs += "-XXLanguage:+InlineClasses"
}