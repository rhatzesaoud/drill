package com.epam.drill.client

import com.epam.drill.common.*
import com.epam.drill.endpoints.agent.*
import com.epam.drill.router.*
import io.ktor.http.*
import io.ktor.locations.*
import io.ktor.server.testing.*

fun TestApplicationEngine.register(agentId: String, payload: AgentRegistrationInfo, token: String) =
    handleRequest(HttpMethod.Post, "/api" + application.locations.href(Routes.Api.Agent.RegisterAgent(agentId))) {
        addHeader(HttpHeaders.Authorization, "Bearer $token")
        setBody(AgentRegistrationInfo.serializer() stringify payload)
    }.run { response.status() to response.content }

fun TestApplicationEngine.addPlugin(agentId: String, payload: PluginId, token: String) =
    handleRequest(HttpMethod.Post, "/api" + application.locations.href(Routes.Api.Agent.AddNewPlugin(agentId))) {
        addHeader(HttpHeaders.Authorization, "Bearer $token")
        setBody(PluginId.serializer() stringify payload)
    }.run { response.status() to response.content }
