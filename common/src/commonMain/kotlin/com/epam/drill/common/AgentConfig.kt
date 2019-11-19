package com.epam.drill.common

import kotlinx.serialization.*

@Serializable
data class AgentConfig(
    val id: String,
    var buildVersion: String,
    var serviceGroupId: String,
    var needSync: Boolean = true,
    var packagesPrefixes: String = "{\"packagesPrefixes\":[]}"
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