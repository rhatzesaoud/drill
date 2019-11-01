package com.epam.drill.plugin.api.end

import com.epam.drill.common.*

interface Sender {
    suspend fun send(agentId: String, buildVersion: String, destination: Any, message: Any)
}
