package com.epam.drill.common

import kotlinx.serialization.*

@Serializable
data class PluginMetadata(
        val id: String,
        var name: String = "",
        var description: String = "",
        var type: String = "",
        var family: Family = Family.INSTRUMENTATION,
        var enabled: Boolean = true,
        var config: String = "",
        var md5Hash: String = "",
        var isNative: Boolean = false
) {
    fun toPluginConfig() = PluginConfig(id, config)
}

enum class Family {
    GENERIC, INSTRUMENTATION
}

@Serializable
data class PluginConfig(
        val id: String,
        val data: String
)