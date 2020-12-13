package com.epam.drill.plugin.api.processing

import com.epam.drill.common.*
import kotlinx.serialization.*

expect abstract class AgentPart<T, A>(
    id: String,
    context: AgentContext
) : AgentPlugin<A> {

    abstract val confSerializer: KSerializer<T>

    var np: NativePart<T>?
    var enabled: Boolean

    fun load(on: Boolean)
    fun unload(unloadReason: UnloadReason)

    abstract override fun initPlugin()
    abstract override fun destroyPlugin(unloadReason: UnloadReason)

    abstract override fun on()
    abstract override fun off()

    fun rawConfig(): String
}

expect abstract class NativePart<T> {
    actual abstract val confSerializer: KSerializer<T>
    fun updateRawConfig(config: PluginConfig)
}

enum class UnloadReason {
    ACTION_FROM_ADMIN, SH
}
