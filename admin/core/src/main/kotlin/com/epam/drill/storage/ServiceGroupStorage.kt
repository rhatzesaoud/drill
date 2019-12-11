package com.epam.drill.storage

import com.epam.drill.common.*
import java.util.concurrent.*

typealias ServiceGroupStorage = ConcurrentHashMap<String, ServiceGroup>

fun ServiceGroupStorage.addAgent(agentId: String, serviceGroup: String) =
    createOrGet(serviceGroup)
        .addAgent(agentId)

fun ServiceGroupStorage.removeAgent(agentId: String, serviceGroup: String) =
    get(serviceGroup)
        ?.addAgent(agentId)


fun ServiceGroupStorage.createOrGet(serviceGroup: String) = get(serviceGroup)
    ?: run {
        val newGroup = ServiceGroup(serviceGroup)
        put(serviceGroup, newGroup)
        newGroup
    }
