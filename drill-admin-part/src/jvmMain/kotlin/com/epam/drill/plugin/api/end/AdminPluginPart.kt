package com.epam.drill.plugin.api.end

import com.epam.drill.common.*
import com.epam.drill.plugin.api.*
import com.epam.drill.plugin.api.message.*
import com.epam.kodux.*

abstract class AdminPluginPart<A>(
    val adminData: AdminData,
    val sender: Sender,
    val store: StoreClient,
    val agentInfo: AgentInfo,
    override val id: String
) : DrillPlugin<A> {
    abstract suspend fun processData(dm: DrillMessage): Any
    abstract suspend fun dropData(): Unit
}
