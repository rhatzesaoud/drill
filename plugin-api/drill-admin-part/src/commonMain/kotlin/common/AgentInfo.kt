package com.epam.drill.common

//TODO remove
data class AgentInfo(
    val id: String,
    val agentType: String,
    val serviceGroup: String = "",
    val buildVersion: String,
    val agentVersion: String = "",
    val name: String = "",
    val environment: String = "",
    val description: String = "",
    val sessionIdHeaderName: String = "",
    val adminUrl: String = "",
    val ipAddress: String = ""
) {
    override fun equals(
        other: Any?
    ): Boolean = other is AgentInfo && id == other.id && buildVersion == other.buildVersion

    override fun hashCode(): Int = 31 * id.hashCode() + buildVersion.hashCode()
}
