package com.epam.drill.agent

import com.epam.drill.client.*
import com.epam.drill.common.*
import com.epam.drill.common.ws.ServiceConfig
import com.epam.drill.endpoints.WsTopic
import com.epam.drill.endpoints.agent.AgentRegistrationInfo
import com.epam.drill.endpoints.requestToken
import com.epam.drill.testdata.*
import com.epam.drill.websockets.AgentMessage
import com.epam.drill.websockets.UiMessage
import io.kotlintest.matchers.types.shouldBeInstanceOf
import io.kotlintest.shouldBe
import io.ktor.http.HttpStatusCode
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readBytes
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.apache.commons.codec.digest.DigestUtils
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder


class ExampleAgentWsTest {
    @get:Rule
    val projectDir = TemporaryFolder()
    private val queue = Channels()
    val appConfig = AppConfig(projectDir)
    private val testApp = appConfig.testApp

    @Test(timeout = 10000)
    fun `Main Example`() {
        val handler = CoroutineExceptionHandler { _, exception ->
            coroutineException = exception
        }
        withTestApplication({ testApp(this, sslPort) }) {
            val token = requestToken()
            //create the 'drill-admin-socket' websocket connection
            handleWebSocketConversation("/ws/drill-admin-socket?token=${token}") { uiIncoming, ut ->
                application.queued(appConfig.wsTopic, queue, uiIncoming)
                //send subscribe event  to get agent status
                ut.send(UiMessage(WsMessageType.SUBSCRIBE, "/get-agent/$agentId", ""))
                queue.getAgent() shouldBe null
                application.launch(handler) {
                    //create the '/agent/attach' websocket connection
                    handleWebSocketConversation("/agent/attach", wsRequestRequiredParams()) { incoming, outgoing ->
                        queue.getAgent()?.status shouldBe AgentStatus.NOT_REGISTERED
                        val (messageType, destination, data) = readAgentMessage(incoming)
                        messageType shouldBe MessageType.MESSAGE
                        destination shouldBe "/agent/config"
                        (ServiceConfig.serializer() parse data).sslPort shouldBe sslPort
                        register(
                            agentId,
                            AgentRegistrationInfo("xz", "ad", "sad"),
                            token
                        ).first shouldBe HttpStatusCode.OK
                        queue.getAgent()?.status shouldBe AgentStatus.ONLINE
                        queue.getAgent()?.status shouldBe AgentStatus.BUSY
                        incoming.receive()
                        outgoing.send(AgentMessage(MessageType.MESSAGE_DELIVERED, "/agent/set-packages-prefixes", ""))
                        incoming.receive()
                        outgoing.send(AgentMessage(MessageType.MESSAGE_DELIVERED, "/agent/load-classes-data", ""))
                        queue.getAgent()?.status shouldBe AgentStatus.ONLINE

                        addPlugin(agentId, pluginT2CM, token)
                        val pluginMetadata = PluginMetadata.serializer() parse (readAgentMessage(incoming)).data
                        incoming.receive().shouldBeInstanceOf<Frame.Binary> { pluginFile ->
                            DigestUtils.md5Hex(pluginFile.readBytes()) shouldBe pluginMetadata.md5Hash
                            queue.getAgent()?.status shouldBe AgentStatus.BUSY
                            `should return BADREQUEST if BUSY`(token)
                            outgoing.send(AgentMessage(MessageType.MESSAGE_DELIVERED, "/plugins/load", ""))
                            queue.getAgent()?.status shouldBe AgentStatus.ONLINE
                            `should return OK if ONLINE`(token)
                        }
                    }
                }.join()
            }
        }
        if (coroutineException != null) {
            throw coroutineException as Throwable
        }
    }
}