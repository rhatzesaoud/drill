package com.epam.drill.e2e

import com.epam.drill.client.*
import com.epam.drill.common.AgentStatus
import com.epam.drill.common.MessageType
import com.epam.drill.common.WsMessageType
import com.epam.drill.common.parse
import com.epam.drill.common.ws.ServiceConfig
import com.epam.drill.endpoints.WsTopic
import com.epam.drill.endpoints.agent.AgentRegistrationInfo
import com.epam.drill.endpoints.requestToken
import com.epam.drill.testdata.AppConfig
import com.epam.drill.testdata.agentId
import com.epam.drill.testdata.coroutineException
import com.epam.drill.testdata.sslPort
import com.epam.drill.websockets.*
import io.kotlintest.shouldBe
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ToggleAgentTest {
    @get:Rule
    val projectDir = TemporaryFolder()
    private val queue = Channels()
    val appConfig = AppConfig(projectDir)
    private val testApp = appConfig.testApp

    @Test(timeout = 10000)
        fun `Toggle Agent Test`() {
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

                        toggleAgent(agentId, token)
                        queue.getAgent()?.status shouldBe AgentStatus.OFFLINE
                        toggleAgent(agentId, token)
                        queue.getAgent()?.status shouldBe AgentStatus.ONLINE

                    }
                }.join()
            }
        }
        if (coroutineException != null) {
            throw coroutineException as Throwable
        }
    }
}