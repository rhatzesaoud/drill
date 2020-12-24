rootProject.name = "drill"

val scriptUrl: String by extra

apply(from = "$scriptUrl/maven-repo.settings.gradle.kts")

pluginManagement {
    val kotlinVersion: String by extra
    plugins {
        kotlin("multiplatform") version kotlinVersion
        kotlin("plugin.serialization") version kotlinVersion
    }
}

include(":common")
include(":plugin-api")
include(":plugin-api:drill-admin-part")
include(":plugin-api:drill-agent-part")
