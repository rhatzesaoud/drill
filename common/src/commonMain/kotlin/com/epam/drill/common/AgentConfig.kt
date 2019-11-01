package com.epam.drill.common

import kotlinx.serialization.*

@Serializable
data class AgentConfig(
    val id: String,
    var buildVersion: String,
    var needSync: Boolean = true,
    var packagesPrefixes: String = "{\"packagesPrefixes\":[]}"
)


@Serializable
data class PackagesPrefixes(
    val packagesPrefixes: List<String> = emptyList()
)

@Serializable
data class PluginId(val pluginId: String)

const val AgentConfigParam = "AgentConfig"
const val NeedSyncParam = "needSync"