package com.epam.drill.e2e

import com.epam.drill.client.*
import com.epam.drill.common.*
import com.epam.drill.common.ws.*
import com.epam.drill.endpoints.*
import com.epam.drill.testdata.*
import io.kotlintest.*
import io.ktor.http.cio.websocket.*
import io.ktor.server.testing.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import org.junit.*
import org.junit.rules.*


abstract class AbstarctE2ETest {
    @get:Rule
    val projectDir = TemporaryFolder()
    val queue = Channels()
    val appConfig = AppConfig(projectDir)
    private val testApp = appConfig.testApp


    fun createSimpleAppWithAgentConnect(block: suspend TestApplicationEngine.(ReceiveChannel<Frame>, SendChannel<Frame>, String) -> Unit) {
        var coroutineException: Throwable? = null
        val handler = CoroutineExceptionHandler { _, exception ->
            coroutineException = exception
        }
        withTestApplication({ testApp(this, sslPort) }) {
            val token = requestToken()
            //create the 'drill-admin-socket' websocket connection
            handleWebSocketConversation("/ws/drill-admin-socket?token=${token}") { uiIncoming, ut ->
                application.queued(appConfig.wsTopic, queue, uiIncoming)
                //send subscribe event  to get agent status
                ut.send(com.epam.drill.websockets.UiMessage(WsMessageType.SUBSCRIBE, "/get-agent/$agentId", ""))
                ut.send(com.epam.drill.websockets.UiMessage(WsMessageType.SUBSCRIBE, "/$agentId/builds", ""))
                queue.getAgent() shouldBe null
                application.launch(handler) {
                    //create the '/agent/attach' websocket connection
                    handleWebSocketConversation("/agent/attach", wsRequestRequiredParams()) { incoming, outgoing ->
                        block(this@withTestApplication, incoming, outgoing, token)
                    }
                }.join()
            }
            if (coroutineException != null) {
                throw coroutineException as Throwable
            }
        }
    }

    suspend fun validateFirstResponseForAgent(agentInput: ReceiveChannel<Frame>) {
        val (messageType, destination, data) = readAgentMessage(agentInput)
        messageType shouldBe MessageType.MESSAGE
        destination shouldBe "/agent/config"
        (ServiceConfig.serializer() parse data).sslPort shouldBe sslPort
    }
}