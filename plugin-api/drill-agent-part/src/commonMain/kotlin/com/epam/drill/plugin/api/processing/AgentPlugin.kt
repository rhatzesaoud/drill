package com.epam.drill.plugin.api.processing

import com.epam.drill.logger.api.*
import com.epam.drill.plugin.api.*

class AgentContext(
    val logging: LoggerFactory
)

interface AgentPlugin<A> : DrillPlugin<A>, Switchable, Lifecycle

interface Switchable {
    fun on()
    fun off()
}

interface Lifecycle {
    fun initPlugin()
    fun destroyPlugin(unloadReason: UnloadReason)
}

interface Instrumenter {
    fun instrument(className: String, initialBytes: ByteArray): ByteArray?
}
