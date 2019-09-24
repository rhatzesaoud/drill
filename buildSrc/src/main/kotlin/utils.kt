import com.google.gson.*
import com.palantir.gradle.gitversion.*
import groovy.lang.*
import org.gradle.api.*
import org.gradle.kotlin.dsl.*
import org.gradle.language.jvm.tasks.*

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
                else -> "$major.${minor.toInt().inc()}.$patch-SNAPSHOT"
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

    tasks {
        val generateVersionJson by registering {
            group = "versioning"
            val versionFile = buildDir.resolve("version.json")
            inputs.dir(".git")
            outputs.file(versionFile)
            doLast {
                val versionInfo = VersionInfo(
                    version = "${project.version}",
                    lastTag = versionDetails.lastTag,
                    commitDistance = versionDetails.commitDistance,
                    gitHash = versionDetails.gitHash
                )
                versionFile.writeText(Gson().toJson(versionInfo))
            }
        }

        withType(ProcessResources::class) {
            from(generateVersionJson)
        }
    }
}
