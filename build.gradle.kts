import java.net.*

plugins {
    base
    kotlin("multiplatform") apply false
    id("com.github.hierynomus.license")
    `maven-publish`
}

val scriptUrl: String by extra

allprojects {
    apply(from = rootProject.uri("$scriptUrl/git-version.gradle.kts"))
}

subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        apply(from = "$scriptUrl/maven-repo.gradle.kts")
    }
}

val licenseFormatSettings by tasks.registering(com.hierynomus.gradle.license.tasks.LicenseFormat::class) {
    source = fileTree(project.projectDir).also {
        include("**/*.kt", "**/*.java", "**/*.groovy")
        exclude("**/.idea")
    }.asFileTree
    headerURI = URI("https://raw.githubusercontent.com/Drill4J/drill4j/develop/COPYRIGHT")
}

tasks["licenseFormat"].dependsOn(licenseFormatSettings)
