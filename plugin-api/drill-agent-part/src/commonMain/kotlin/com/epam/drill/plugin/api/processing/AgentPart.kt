package com.epam.drill.plugin.api.processing

import com.epam.drill.logger.api.*

@Suppress("unused")
abstract class AgentPart<A>(
    val id: String,
    val context: AgentContext,
    private val sender: Sender,
    @Suppress("UNUSED_PARAMETER")
    logging: LoggerFactory
) : AgentPlugin<A> {
    fun send(message: String) = sender.send(id, message)

    open fun updateRawConfig(data: String) = Unit

    //TODO remove from API - this is only used in agent
    open fun isEnabled(): Boolean = true
    //TODO remove from API - this is only used in agent
    open fun setEnabled(enabled: Boolean) = Unit
    //TODO remove from API - this is only used in agent
    open fun load(on: Boolean) {
        initPlugin()
        if (on) {
            on()
        }
    }
    //TODO remove from API - this is only used in agent
    open fun unload(unloadReason: UnloadReason) {
        off()
        destroyPlugin(unloadReason)
    }
}
