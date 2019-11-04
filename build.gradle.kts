import org.jetbrains.kotlin.gradle.tasks.*

plugins {
    `build-scan` version "3.0"
    id("com.epam.drill.version.plugin")
}

buildScan {
    termsOfServiceUrl = "https://gradle.com/terms-of-service"
    termsOfServiceAgree = "yes"
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
