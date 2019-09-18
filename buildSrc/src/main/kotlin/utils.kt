import com.google.gson.*
import com.palantir.gradle.gitversion.*
import groovy.lang.*
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.kotlin.dsl.*
import java.io.*

val serializationRuntimeVersion = "0.12.0"

val ktorVersion = "1.2.4"

val drillCommonLibVersion = "0.1.1"
val drillPluginApiVersion = "0.3.0"


val Project.generatedResourcesDir: File
    get() = buildDir.resolve("generated-resources")


val Project.versionDetails: VersionDetails
    get() {
        val versionDetails: Closure<VersionDetails> by extra
        return versionDetails()
    }
const val DEFAULT_VERSION = "0.3.0-SNAPSHOT"

private fun VersionDetails.toProjectVersion() = object {
    val versionRegex = Regex("v(\\d+)\\.(\\d+)\\.(\\d+)")

    override fun toString(): String = when (val matched = versionRegex.matchEntire(lastTag)) {
        is MatchResult -> {
            val (_, major, minor, patch) = matched.groupValues
            when (commitDistance) {
                0 -> "$major.$minor.$patch"
                else -> "$major.${minor.toInt() + 1}.$patch-SNAPSHOT"
            }
        }
        else -> when {
            gitHash.startsWith(lastTag) -> DEFAULT_VERSION
            else -> Project.DEFAULT_VERSION
        }
    }
}

data class VersionInfo(
    val version: String,
    val lastTag: String,
    val commitDistance: Int,
    val gitHash: String
)

fun Project.setupVersion() {
    apply<GitVersionPlugin>()

    version = versionDetails.toProjectVersion()

    val generateVersionJson by tasks.registering {
        group = "versioning"
        outputs.upToDateWhen { false }
        outputs.dir(generatedResourcesDir)
        doLast {
            val versionInfo = VersionInfo(
                version = "${project.version}",
                lastTag = versionDetails.lastTag,
                commitDistance = versionDetails.commitDistance,
                gitHash = versionDetails.gitHash
            )
            val versionFile = generatedResourcesDir.resolve("version.json")
            versionFile.writeText(Gson().toJson(versionInfo))
        }
    }

    withConvention(JavaPluginConvention::class) {
        sourceSets.named("main") {
            output.dir(mapOf("builtBy" to generateVersionJson), generatedResourcesDir)
        }
    }
}
