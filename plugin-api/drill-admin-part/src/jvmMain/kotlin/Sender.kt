package com.epam.drill.plugin.api.end

interface Sender {
    suspend fun send(agentId: String, buildVersion: String, destination: Any, message: Any)
}
