package com.epam.drill.client

import com.epam.drill.agentmanager.AgentInfoWebSocket
import com.epam.drill.agentmanager.AgentInfoWebSocketSingle
import com.epam.drill.common.*
import com.epam.drill.dataclasses.Notification
import com.epam.drill.endpoints.WsTopic
import com.epam.drill.plugins.PluginWebSocket
import com.epam.drill.router.WsRoutes
import io.ktor.application.Application
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import kotlinx.serialization.list

class Channels {

    val agentChannel = Channel<AgentInfoWebSocketSingle?>()
    val agentBuildsChannel = Channel<Set<AgentBuildVersionJson>?>()
    val buildsChannel = Channel<Set<BuildSummary>?>()
    val agentsChannel = Channel<Set<AgentInfoWebSocket>?>()
    val allPluginsChannel = Channel<Set<PluginWebSocket>?>()
    val notificationsChannel = Channel<Set<Notification>?>()
    val agentPluginInfoChannel = Channel<Set<PluginWebSocket>?>()

    suspend fun getAgent() = agentChannel.receive()
    suspend fun getAgentBuilds() = agentBuildsChannel.receive()
    suspend fun getBuilds() = buildsChannel.receive()
    suspend fun getAllAgents() = agentsChannel.receive()
    suspend fun getAllPluginsInfo() = allPluginsChannel.receive()
    suspend fun getNotifications() = notificationsChannel.receive()
    suspend fun getAgentPluginInfo() = agentPluginInfoChannel.receive()
}

@UseExperimental(ExperimentalCoroutinesApi::class)
fun Application.queued(wsTopic: WsTopic, queue: Channels, incoming: ReceiveChannel<Frame>) = this.launch {
    incoming.consumeEach {
        when (it) {
            is Frame.Text -> {
                val parseJson = json.parseJson(it.readText())
                val url = (parseJson as JsonObject)[WsReceiveMessage::destination.name]!!.content!!

                this@queued.apply {
                    wsTopic {
                        val (_, type) = getParams(url)
                        launch {
                            when (type) {
                                is WsRoutes.GetAllAgents -> {
                                    val content =
                                        parseJson[WsReceiveMessage::message.name]!!.toString()
                                    if (content != "\"\"") {
                                        val element =
                                            setOf(AgentInfoWebSocket.serializer() parse content)
                                        queue.agentsChannel.send(element)
                                    } else {
                                        queue.agentsChannel.send(null)
                                    }
                                }
                                is WsRoutes.GetAgent -> {
                                    val content =
                                        parseJson[WsReceiveMessage::message.name]!!.toString()
                                    if (content != "\"\"") {
                                        val element =
                                            AgentInfoWebSocketSingle.serializer() parse content
                                        queue.agentChannel.send(element)
                                    } else {
                                        queue.agentChannel.send(null)
                                    }
                                }
                                is WsRoutes.GetAgentBuilds -> {
                                    val content =
                                        parseJson[WsReceiveMessage::message.name]!!.toString()
                                    if (content != "\"\"") {
                                        val element =
                                            setOf(AgentBuildVersionJson.serializer() parse content)
                                        queue.agentBuildsChannel.send(element)
                                    } else {
                                        queue.agentBuildsChannel.send(null)
                                    }
                                }
                                is WsRoutes.GetAllPlugins -> {
                                    val content =
                                        parseJson[WsReceiveMessage::message.name]!!.toString()
                                    if (content != "\"\"") {
                                        val element =
                                            setOf(PluginWebSocket.serializer() parse content)
                                        queue.allPluginsChannel.send(element)
                                    } else {
                                        queue.allPluginsChannel.send(null)
                                    }
                                }

                                is WsRoutes.GetBuilds -> {
                                    val content =
                                        parseJson[WsReceiveMessage::message.name]!!.toString()
                                    if (content != "\"\"") {
                                        val element =
                                            (BuildSummary.serializer().list parse content).toSet()
                                        queue.buildsChannel.send(element)
                                    } else {
                                        queue.buildsChannel.send(null)
                                    }
                                }

                                is WsRoutes.GetNotifications -> {
                                    val content =
                                        parseJson[WsReceiveMessage::message.name]!!.toString()
                                    if (content != "\"\"") {
                                        val element =
                                            setOf(Notification.serializer() parse content)
                                        queue.notificationsChannel.send(element)
                                    } else {
                                        queue.notificationsChannel.send(null)
                                    }
                                }

                                is WsRoutes.GetPluginConfig -> {
                                }
                                is WsRoutes.GetPluginInfo -> {
                                    val content =
                                        parseJson[WsReceiveMessage::message.name]!!.toString()
                                    if (content != "\"\"") {
                                        val element =
                                            setOf(PluginWebSocket.serializer() parse content)
                                        queue.agentPluginInfoChannel.send(element)
                                    } else {
                                        queue.agentPluginInfoChannel.send(null)
                                    }
                                }
                                else -> {
                                }
                            }
                        }
                    }
                }
            }
            else -> throw RuntimeException(" read not FRAME.TEXT frame.")
        }
    }

}