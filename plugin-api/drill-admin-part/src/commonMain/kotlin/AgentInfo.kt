package com.epam.drill.common

import kotlinx.serialization.*

@Serializable
data class AgentInfo(
    val id: String,
    var name: String,
    var status: AgentStatus,
    var serviceGroup: String = "",
    var environment: String = "",
    var description: String,
    var buildVersion: String,
    var agentType: AgentType,
    val agentVersion: String,
    var sessionIdHeaderName: String = "",
    val adminUrl: String = "",
    var ipAddress: String = "",
    val plugins: MutableSet<PluginMetadata> = mutableSetOf()
) {
    override fun equals(other: Any?): Boolean {
        return other is AgentInfo && id == other.id && buildVersion == other.buildVersion
    }

    override fun hashCode() = (id to buildVersion).hashCode()
}

enum class AgentStatus {
    NOT_REGISTERED,
    ONLINE,
    OFFLINE,
    BUSY;
}
