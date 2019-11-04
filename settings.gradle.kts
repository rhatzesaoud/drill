rootProject.name = "drill"
include(":admin")
include(":admin:core")
include(":admin:test-framework")
include(":agent")
include(":agent:proxy-agent")
include(":agent:pt-runner")
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