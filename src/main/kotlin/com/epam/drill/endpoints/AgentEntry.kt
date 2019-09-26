package com.epam.drill.endpoints

import com.epam.drill.common.*
import com.epam.drill.endpoints.agent.*
import com.epam.drill.plugin.api.end.*

class AgentEntry(
    val agent: AgentInfo,
    val agentSession: AgentWsSession,
    var instance: MutableMap<String, AdminPluginPart<*>> = mutableMapOf()
)