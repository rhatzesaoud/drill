package com.epam.drill.plugin.api.end

import com.epam.drill.common.*
import com.epam.drill.plugin.api.*

@Suppress("unused")
abstract class AdminPluginPart<A>(
    val id: String,
    val agentInfo: AgentInfo,
    val adminData: AdminData,
    val sender: Sender
) {
    open suspend fun initialize() = Unit

    abstract suspend fun doAction(action: A): Any

    abstract fun parseAction(rawAction: String): A

    suspend fun doRawAction(rawAction: String): Any = doAction(parseAction(rawAction))

    open suspend fun processData(
        instanceId: String,
        content: String
    ): Any = Unit

    open suspend fun applyPackagesChanges() = Unit

    @Deprecated("", replaceWith = ReplaceWith(""))
    open suspend fun dropData() = Unit
}
