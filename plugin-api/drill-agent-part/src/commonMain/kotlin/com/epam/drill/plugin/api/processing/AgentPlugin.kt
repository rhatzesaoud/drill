package com.epam.drill.plugin.api.processing

interface AgentContext {
    operator fun invoke(): String?
    operator fun get(key: String): String?
}

interface Sender {
    fun send(pluginId: String, message: String)
}

interface AgentPlugin<A> : DrillPlugin<A>, Switchable, Lifecycle

interface DrillPlugin<A> {
    suspend fun doAction(action: A): Any
    fun parseAction(rawAction: String): A
    suspend fun doRawAction(rawAction: String): Any = doAction(parseAction(rawAction))
}

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

enum class UnloadReason {
    ACTION_FROM_ADMIN, SH
}
