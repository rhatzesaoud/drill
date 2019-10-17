package com.epam.drill.client

import com.epam.drill.agentmanager.*
import com.epam.drill.common.*
import com.epam.drill.common.ws.*
import com.epam.drill.dataclasses.*
import com.epam.drill.endpoints.*
import com.epam.drill.plugins.*
import com.epam.drill.router.*
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

class AdminUiChannels {

    private val agentChannel = Channel<AgentInfoWebSocketSingle?>()
    private val agentBuildsChannel = Channel<Set<AgentBuildVersionJson>?>()
    private val buildsChannel = Channel<Set<BuildSummaryWebSocket>?>()
    private val agentsChannel = Channel<Set<AgentInfoWebSocket>?>()
    private val allPluginsChannel = Channel<Set<PluginWebSocket>?>()
    private val notificationsChannel = Channel<Set<Notification>?>()
    private val agentPluginInfoChannel = Channel<Set<PluginWebSocket>?>()

    suspend fun getAgent() = agentChannel.receive()
    suspend fun getAgentBuilds() = agentBuildsChannel.receive()
    suspend fun getBuilds() = buildsChannel.receive()
    suspend fun getAllAgents() = agentsChannel.receive()
    suspend fun getAllPluginsInfo() = allPluginsChannel.receive()
    suspend fun getNotifications() = notificationsChannel.receive()
    suspend fun getAgentPluginInfo() = agentPluginInfoChannel.receive()

    @UseExperimental(ExperimentalCoroutinesApi::class)
    fun Application.queued(wsTopic: WsTopic, incoming: ReceiveChannel<Frame>) = this.launch {
        incoming.consumeEach {
            when (it) {
                is Frame.Text -> {
                    val parseJson = json.parseJson(it.readText()) as JsonObject
                    val url = parseJson[WsReceiveMessage::destination.name]!!.content
                    val content = parseJson[WsReceiveMessage::message.name]!!.toString()
                    val (_, type) = wsTopic.getParams(url)
                    this@queued.launch {
                        val notEmptyResponse = content != "\"\""
                        when (type) {
                            is WsRoutes.GetAllAgents -> {
                                if (notEmptyResponse) {
                                    agentsChannel.send((AgentInfoWebSocket.serializer().set parse content))
                                } else {
                                    agentsChannel.send(null)
                                }
                            }
                            is WsRoutes.GetAgent -> {
                                if (notEmptyResponse) {
                                    agentChannel.send(AgentInfoWebSocketSingle.serializer() parse content)
                                } else {
                                    agentChannel.send(null)
                                }
                            }
                            is WsRoutes.GetAgentBuilds -> {
                                if (notEmptyResponse) {
                                    agentBuildsChannel.send((AgentBuildVersionJson.serializer().set parse content))
                                } else {
                                    agentBuildsChannel.send(null)
                                }
                            }
                            is WsRoutes.GetAllPlugins -> {
                                if (notEmptyResponse) {
                                    allPluginsChannel.send((PluginWebSocket.serializer().set parse content))
                                } else {
                                    allPluginsChannel.send(null)
                                }
                            }

                            is WsRoutes.GetBuilds -> {
                                if (notEmptyResponse) {
                                    buildsChannel.send((BuildSummaryWebSocket.serializer().set parse content))
                                } else {
                                    buildsChannel.send(null)
                                }
                            }

                            is WsRoutes.GetNotifications -> {
                                if (notEmptyResponse) {
                                    notificationsChannel.send(Notification.serializer().set parse content)
                                } else {
                                    notificationsChannel.send(null)
                                }
                            }

                            is WsRoutes.GetPluginConfig -> {
                            }
                            is WsRoutes.GetPluginInfo -> {
                                if (notEmptyResponse) {
                                    agentPluginInfoChannel.send(PluginWebSocket.serializer().set parse content)
                                } else {
                                    agentPluginInfoChannel.send(null)
                                }
                            }
                        }
                    }
                }
                else -> throw RuntimeException(" read not FRAME.TEXT frame.")
            }
        }

    }
}

class AgentChannels {

    val serviceConfig = Channel<ServiceConfig?>()

    suspend fun getServiceConfig() = serviceConfig.receive()
}

