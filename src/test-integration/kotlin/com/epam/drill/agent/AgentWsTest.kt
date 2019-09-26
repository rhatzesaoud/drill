package com.epam.drill.agent

import com.epam.drill.agentmanager.*
import com.epam.drill.common.*
import com.epam.drill.common.ws.*
import com.epam.drill.endpoints.AgentManager
import com.epam.drill.endpoints.requestToken
import com.epam.drill.kodein.*
import com.epam.drill.plugin.api.end.*
import com.epam.drill.plugins.*
import com.epam.drill.router.*
import com.epam.drill.websockets.AgentMessage
import com.epam.drill.websockets.UiMessage
import io.kotlintest.*
import io.kotlintest.matchers.types.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.locations.*
import io.ktor.server.testing.*
import io.mockk.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.serialization.*
import kotlinx.serialization.cbor.*
import org.apache.commons.codec.digest.*
import org.junit.*
import org.kodein.di.*
import org.kodein.di.generic.*
import java.io.*


private val agentId = "testAgent"
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
                            doHttpCall(this@withTestApplication, this, token, HttpStatusCode.BadRequest)
                            outgoing.send(AgentMessage(MessageType.MESSAGE_DELIVERED, "/plugins/load", ""))
                            readGetAgentTopicMessage(uiIncoming).status shouldBe AgentStatus.ONLINE
                            doHttpCall(this@withTestApplication, this, token, HttpStatusCode.OK)
                        }
                    }
                }.join()
            }
        }
    }

    private fun doHttpCall(
        testApplicationEngine: TestApplicationEngine,
        testApplicationCall: TestApplicationCall,
        token: String,
        badRequest: HttpStatusCode
    ) {
        val (status, content) =
            testApplicationEngine.handleRequest(
                HttpMethod.Post,
                testApplicationCall.locations.href(Routes.Api.UpdateAgentConfig(agentId))
            ) {
                addHeader(HttpHeaders.Authorization, "Bearer $token")
                setBody(
                    AgentInfoWebSocketSingle.serializer() stringify ai.toAgentInfoWebSocket().copy(
                        name = "modified"
                    )
                )
            }.run {
                response.status() to response.content
            }

        status shouldBe badRequest
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

    builder.bind<AgentManager>() with builder.eagerSingleton {
        spyk(AgentManager(kodein)).apply {
            val thiz = this
            coEvery { agentConfiguration(agentId, "0.1.0") } returns ai
            coEvery { thiz.getOrNull(ai.id) } returns ai
            coEvery { updateAgent(agentId, any()) } returns Unit
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