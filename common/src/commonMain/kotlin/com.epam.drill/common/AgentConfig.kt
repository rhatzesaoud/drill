package com.epam.drill.common

import kotlinx.serialization.*

@Serializable
data class AgentConfig(
    val id: String,
    val instanceId: String,
    val buildVersion: String,
    val serviceGroupId: String,
    val agentType: AgentType,
    val agentVersion: String = "",
    val needSync: Boolean = true,
    val packagesPrefixes: PackagesPrefixes = PackagesPrefixes()
)


@Serializable
data class PackagesPrefixes(
    val packagesPrefixes: List<String> = emptyList()
)

@Serializable
data class PluginId(val pluginId: String)

@Serializable
data class TogglePayload(val pluginId: String, val forceValue: Boolean? = null)

const val AgentConfigParam = "AgentConfig"
const val NeedSyncParam = "needSync"
