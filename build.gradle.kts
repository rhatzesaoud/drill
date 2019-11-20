import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    id("com.epam.drill.version.plugin")
}

subprojects {
    apply(plugin = "com.epam.drill.version.plugin")
    tasks.withType<KotlinCompile> {
        kotlinOptions.allWarningsAsErrors = true
    }
    tasks.withType<KotlinNativeCompile> {
        kotlinOptions.allWarningsAsErrors = true
    }
}
