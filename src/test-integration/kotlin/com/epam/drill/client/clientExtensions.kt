package com.epam.drill.client

import com.epam.drill.agentmanager.AgentInfoWebSocketSingle
import com.epam.drill.agentmanager.toAgentInfoWebSocket
import com.epam.drill.common.PluginId
import com.epam.drill.common.stringify
import com.epam.drill.endpoints.agent.AgentRegistrationInfo
import com.epam.drill.router.Routes
import com.epam.drill.testdata.agentId
import com.epam.drill.testdata.ai
import io.kotlintest.shouldBe
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.locations.locations
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody

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

fun TestApplicationEngine.unRegister(agentId: String, token: String) =
    handleRequest(HttpMethod.Post, "/api" + application.locations.href(Routes.Api.Agent.UnregisterAgent(agentId))) {
        addHeader(HttpHeaders.Authorization, "Bearer $token")
    }.run { response.status() to response.content }

fun TestApplicationEngine.unLoadPlugin(agentId: String, payload: PluginId, token: String) {
    handleRequest(
        HttpMethod.Post,
        "/api" + application.locations.href(Routes.Api.Agent.UpdatePlugin(agentId, payload.pluginId))
    ) {
        addHeader(HttpHeaders.Authorization, "Bearer $token")
    }.run { response.status() to response.content }
}

fun TestApplicationEngine.togglePlugin(agentId: String, pluginId: PluginId, token: String) {
    handleRequest(
        HttpMethod.Post,
        "/api" + application.locations.href(Routes.Api.Agent.TogglePlugin(agentId, pluginId.pluginId))
    ) {
        addHeader(HttpHeaders.Authorization, "Bearer $token")
    }.run { response.status() to response.content }
}

fun TestApplicationEngine.toggleAgent(agentId: String, token: String) {
    handleRequest(HttpMethod.Post, "/api" + application.locations.href(Routes.Api.Agent.AgentToggleStandby(agentId))) {
        addHeader(HttpHeaders.Authorization, "Bearer $token")
    }.run { response.status() to response.content }
}


fun TestApplicationEngine.`should return BADREQUEST if BUSY`(token: String) {
    doHttpCall(token, HttpStatusCode.BadRequest)
}

fun TestApplicationEngine.`should return OK if ONLINE`(token: String) {
    doHttpCall(token, HttpStatusCode.OK)
}

fun TestApplicationEngine.doHttpCall(token: String, exceptedStatus: HttpStatusCode) {
    val status =
        this.handleRequest(
            HttpMethod.Post,
            application.locations.href(Routes.Api.UpdateAgentConfig(agentId))
        ) {
            addHeader(HttpHeaders.Authorization, "Bearer $token")
            setBody(
                AgentInfoWebSocketSingle.serializer() stringify ai.toAgentInfoWebSocket().copy(
                    name = "modified"
                )
            )
        }.run { response.status() }

    status shouldBe exceptedStatus
}