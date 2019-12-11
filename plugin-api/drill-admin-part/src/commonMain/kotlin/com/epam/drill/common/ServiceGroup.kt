package com.epam.drill.common

import kotlin.jvm.*

data class ServiceGroup(
    val id: String,
    val name: String = "",
    val description: String = "",
    val group: String = "",
    val agentIds: MutableSet<String> = mutableSetOf()
) {
    @Synchronized
    fun addAgent(agentId: String) {
        agentIds.add(agentId)
    }

    @Synchronized
    fun removeAgent(agentId: String) {
        agentIds.remove(agentId)
    }
}