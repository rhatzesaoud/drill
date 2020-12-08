package com.epam.drill.plugin.api.end

import com.epam.drill.common.*
import com.epam.drill.plugin.api.*
import com.epam.drill.plugin.api.message.*
import com.epam.kodux.*

@Suppress("unused")
abstract class AdminPluginPart<A>(
    val adminData: AdminData,
    val sender: Sender,
    val store: StoreClient,
    val agentInfo: AgentInfo,
    override val id: String
) : DrillPlugin<A> {
    @Suppress("DEPRECATION")
    open suspend fun processData(
        instanceId: String,
        content: String
    ): Any = processData(DrillMessage(content = content))

    @Deprecated("", replaceWith = ReplaceWith("processData(instanceId, content)"))
    open suspend fun processData(dm: DrillMessage): Any = Unit //TODO remove

    open suspend fun applyPackagesChanges() = Unit

    open suspend fun initialize() = Unit

    @Deprecated("", replaceWith = ReplaceWith(""))
    open suspend fun dropData() = Unit
}
