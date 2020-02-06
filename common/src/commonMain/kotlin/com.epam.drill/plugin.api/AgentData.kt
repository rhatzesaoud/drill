package com.epam.drill.plugin.api

interface AgentData {
    val classMap: Map<String, ByteArray>
}

data class PluginPayload(
    val pluginId: String,
    val agentData: AgentData
)