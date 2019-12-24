rootProject.name = "drill"
include(":agent")
include(":agent:core")
include(":agent:dotnet")
include(":agent:java")
include(":agent:java:proxy-agent")
include(":agent:java:pt-runner")
include(":agent:util")


include(":common")
include(":plugin-api")
include(":plugin-api:drill-admin-part")
include(":plugin-api:drill-agent-part")
buildCache {
    local<DirectoryBuildCache> {
        directory = File(rootDir, "build-cache")
        removeUnusedEntriesAfterDays = 30
    }
}

enableFeaturePreview("GRADLE_METADATA")