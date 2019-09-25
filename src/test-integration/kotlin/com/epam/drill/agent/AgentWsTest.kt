package com.epam.drill.agent

import com.epam.drill.agentmanager.AgentInfoWebSocketSingle
import com.epam.drill.common.*
import com.epam.drill.common.ws.ServiceConfig
import com.epam.drill.endpoints.AgentManager
import com.epam.drill.endpoints.requestToken
import com.epam.drill.kodein.AppBuilder
import com.epam.drill.kodein.KodeinConf
import com.epam.drill.plugin.api.end.AdminPluginPart
import com.epam.drill.plugins.AgentPartFiles
import com.epam.drill.plugins.Plugin
import com.epam.drill.plugins.Plugins
import com.epam.drill.websockets.AgentMessage
import com.epam.drill.websockets.UiMessage
import io.kotlintest.fail
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import io.ktor.http.cio.websocket.readText
import io.ktor.server.testing.TestApplicationRequest
import io.ktor.server.testing.withTestApplication
import io.mockk.coEvery
import io.mockk.spyk
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.dumps
import org.apache.commons.codec.digest.DigestUtils
import org.junit.Test
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import java.io.File


private val agentId = "testAgent"

class AgentWsTest {
    @Test
    fun init() {
        val sslPort = "8443"
        AppBuilder.withKModule {
            kodeinModule("addition") {
                configuration(this, this@withKModule)
            }
        }

        withTestApplication({ testApp(this, sslPort) }) {
            val token = requestToken()
            //create the 'drill-admin-socket' websocket connection
            handleWebSocketConversation("/ws/drill-admin-socket?token=${token}") { uiIncoming, ut ->
                println()
                //send subscribe event  to get agent status
                ut.send(UiMessage(WsMessageType.SUBSCRIBE, "/get-agent/$agentId", ""))
                //first time agent is always READY (mockk). Emulate case when agent is already connected to admin
                readGetAgentTopicMessage(uiIncoming).status shouldBe AgentStatus.ONLINE

                application.launch {
                    //create the '/agent/attach' websocket connection
                    handleWebSocketConversation("/agent/attach", wsRequestRequiredParams()) { incoming, outgoing ->

                        val (messageType, destination, data) = readAgentMessage(incoming)
                        messageType shouldBe MessageType.MESSAGE
                        destination shouldBe "/agent/config"
                        (ServiceConfig.serializer() parse data).sslPort shouldBe sslPort

                        readGetAgentTopicMessage(uiIncoming).status shouldBe AgentStatus.ONLINE

                        val pluginMetadata = PluginMetadata.serializer() parse (readAgentMessage(incoming)).data
                        incoming.receive().shouldBeInstanceOf<Frame.Binary> { pluginFile ->
                            DigestUtils.md5Hex(pluginFile.readBytes()) shouldBe pluginMetadata.md5Hash
                            readGetAgentTopicMessage(uiIncoming).status shouldBe AgentStatus.BUSY
                            outgoing.send(AgentMessage(MessageType.MESSAGE_DELIVERED, "/plugins/load", ""))
                            readGetAgentTopicMessage(uiIncoming).status shouldBe AgentStatus.ONLINE
                        }
                    }
                }.join()
            }
        }
    }

    suspend fun readAgentMessage(incoming: ReceiveChannel<Frame>): Message {
        val text = incoming.receive() as? Frame.Text ?: fail("should be Frame.Text")
        return Message.serializer() parse text.readText()
    }

    private suspend fun readGetAgentTopicMessage(incoming: ReceiveChannel<Frame>): AgentInfoWebSocketSingle {
        val wsMessage = WsMessage.serializer() parse (incoming.receive() as Frame.Text).readText()
        val agentInfoWebSocketSingle = AgentInfoWebSocketSingle.serializer() parse wsMessage.message
        return agentInfoWebSocketSingle
    }
}


private fun configuration(builder: Kodein.Builder, kodeinConf: KodeinConf) {
    val pluginMetadata = PluginMetadata(
        id = "test",
        name = "test",
        description = "test",
        type = "test",
        family = Family.INSTRUMENTATION,
        enabled = true,
        config = "config",
        md5Hash = "",
        isNative = false

    )
    builder.bind<AgentManager>() with builder.eagerSingleton {
        val ai = AgentInfo(
            id = agentId,
            name = agentId,
            status = AgentStatus.ONLINE,
            groupName = "",
            description = "",
            buildVersion = "",
            buildAlias = "",
            adminUrl = "",
            plugins = mutableSetOf(
                pluginMetadata
            ),
            buildVersions = mutableSetOf()
        )
        spyk(AgentManager(kodein)).apply {
            val thiz = this
            coEvery { agentConfiguration(agentId, "0.1.0") } returns ai
            coEvery { thiz[ai.id] } returns ai
        }
    }
    builder.bind<Plugins>() with builder.eagerSingleton {
        val plugins = Plugins()
        val pluginClass = AdminPluginPart::class.java

        plugins["test"] = Plugin(
            pluginClass,
            AgentPartFiles(File(this::class.java.getResource("/likejar.jar").toURI())),
            pluginMetadata
        )
        plugins
    }
}

private fun wsRequestRequiredParams(): TestApplicationRequest.() -> Unit {
    return {
        this.addHeader(
            AgentConfigParam,
            Cbor.dumps(AgentConfig.serializer(), AgentConfig(agentId, "0.1.0", true))
        )
        this.addHeader(NeedSyncParam, "true")
    }
}