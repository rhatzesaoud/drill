rootProject.name = "drill"
include(":admin")
include(":admin:core")
include(":admin:test-framework")
include(":admin:test-framework:test-data")
include(":admin:test-framework:test-plugin")
include(":admin:test-framework:test-plugin:admin-part")
include(":admin:test-framework:test-plugin:agent-part")
include(":agent")
include(":agent:core")
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