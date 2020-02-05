import org.apache.tools.ant.taskdefs.condition.*
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.mpp.*

val isDevMode = System.getProperty("idea.active") == "true"

val presetName: String =
    when {
        Os.isFamily(Os.FAMILY_MAC) -> "macosX64"
        Os.isFamily(Os.FAMILY_UNIX) -> "linuxX64"
        Os.isFamily(Os.FAMILY_WINDOWS) -> "mingwX64"
        else -> throw RuntimeException("Target ${System.getProperty("os.name")} is not supported")
    }

fun KotlinMultiplatformExtension.currentTarget(
    name: String = presetName,
    config: KotlinNativeTarget.() -> Unit = {}
): KotlinNativeTarget {
    val createTarget =
        (presets.getByName(presetName) as KotlinNativeTargetWithTestsPreset).createTarget(name)
    targets.add(createTarget)
    config(createTarget)
    return createTarget
}
