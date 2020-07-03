package com.epam.drill.plugin.api.processing

import kotlinx.coroutines.*


actual abstract class AgentPart<T, A> actual constructor(
    override val id: String,
    context: AgentContext
) : AgentPlugin<A> {

    actual abstract val confSerializer: kotlinx.serialization.KSerializer<T>

    actual var np: NativePart<T>? = null
    actual var enabled: Boolean = false

    val config: T get() = confSerializer parse rawConfig!!

    private var rawConfig: String? = null

    actual fun load(on: Boolean) {
        initPlugin()
        takeIf { on }?.on()
    }

    actual fun unload(unloadReason: UnloadReason) {
        off()
        destroyPlugin(unloadReason)
    }

    actual abstract override fun initPlugin()

    actual abstract override fun destroyPlugin(unloadReason: UnloadReason)

    actual abstract override fun on()

    actual abstract override fun off()

    fun send(message: String) {
        Sender.sendMessage(id, message)
    }

    open fun updateRawConfig(configs: String) {
        rawConfig = configs
    }

    actual fun rawConfig(): String = confSerializer stringify config!!

    //TODO figure out how to handle suspend from the agent
    fun doRawActionBlocking(rawAction: String) = runBlocking<Unit> {
        doRawAction(rawAction)
    }
}
