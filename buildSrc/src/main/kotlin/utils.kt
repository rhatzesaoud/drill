import com.palantir.gradle.gitversion.*
import groovy.lang.*
import org.gradle.api.*
import org.gradle.kotlin.dsl.*

const val serializationRuntimeVersion = "0.12.0"

const val coroutinesVersion = "1.3.0"

const val drillCommonLibVersion = "0.1.1"

const val drillJvmApiLibVersion = "0.1.0"

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
                else -> "$major.${minor + 1}.$patch-SNAPSHOT"
            }
        }
        else -> when {
            gitHash.startsWith(lastTag) -> DEFAULT_VERSION
            else -> Project.DEFAULT_VERSION
        }
    }
}

fun Project.setupVersion() {
    apply<GitVersionPlugin>()

    version = versionDetails.toProjectVersion()
}
