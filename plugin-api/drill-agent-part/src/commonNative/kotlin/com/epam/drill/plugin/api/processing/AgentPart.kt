package com.epam.drill.plugin.api.processing

import com.epam.drill.common.*
import kotlinx.serialization.*

actual abstract class AgentPart<T, A> actual constructor(
    override val id: String,
    context: AgentContext
) : AgentPlugin<A> {

    actual abstract val confSerializer: KSerializer<T>

    actual var np: NativePart<T>? = null
    actual var enabled: Boolean = false

    abstract suspend fun isEnabled(): Boolean

    abstract suspend fun setEnabled(value: Boolean)

    actual open fun load(on: Boolean) {
        initPlugin()
        takeIf { on }?.on()
    }

    actual open fun unload(unloadReason: UnloadReason) {
        off()
        destroyPlugin(unloadReason)
    }

    abstract fun updateRawConfig(config: PluginConfig)

    actual fun rawConfig(): String = np?.run {
        confSerializer stringify config
    } ?: ""
}
