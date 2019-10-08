package com.epam.drill.agent

import com.epam.drill.*
import com.epam.drill.agentmanager.*
import com.epam.drill.client.*
import com.epam.drill.common.*
import com.epam.drill.common.ws.*
import com.epam.drill.endpoints.agent.*
import com.epam.drill.endpoints.requestToken
import com.epam.drill.jwt.config.*
import com.epam.drill.kodein.*
import com.epam.drill.router.*
import com.epam.drill.websockets.AgentMessage
import com.epam.drill.websockets.UiMessage
import com.epam.kodux.*
import io.kotlintest.*
import io.kotlintest.matchers.types.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.locations.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.serialization.*
import kotlinx.serialization.cbor.*
import kotlinx.serialization.json.*
import org.apache.commons.codec.digest.*
import org.junit.*
import org.junit.rules.*
import org.kodein.di.generic.*


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
    @get:Rule
    val projectDir = TemporaryFolder()
    val testApp: Application.(String) -> Unit = { sslPort ->
        (environment.config as MapApplicationConfig).apply {
            put("ktor.deployment.sslPort", sslPort)
            put("ktor.dev", "true")

        }
        install(Locations)
        install(WebSockets)
        install(Authentication) {
            jwt {
                realm = "Drill4J app"
                verifier(JwtConfig.verifier)
                validate {
                    it.payload.getClaim("id").asInt()?.let(userSource::findUserById)
                }
            }
        }
        kodeinApplication(AppBuilder {
            withKModule { kodeinModule("storage", storage) }
            withKModule { kodeinModule("wsHandler", wsHandler) }
            withKModule { kodeinModule("handlers", handlers) }
            withKModule { kodeinModule("pluginServices", pluginServices) }
            val baseLocation = projectDir.newFolder("xs").resolve("agent")
            withKModule {
                kodeinModule("addition") {
                    bind<StoreManger>() with eagerSingleton {
                        StoreManger(baseLocation)
                    }
                }
            }
        })
    }
    var ex: Throwable? = null

    @Test(timeout=5000)
    fun end2end() {
        val sslPort = "8443"
        val handler = CoroutineExceptionHandler { _, exception ->
            ex = exception
        }
        withTestApplication({
            testApp(this, sslPort)
        }) {
            val token = requestToken()
            //create the 'drill-admin-socket' websocket connection
            handleWebSocketConversation("/ws/drill-admin-socket?token=${token}") { uiIncoming, ut ->
                //send subscribe event  to get agent status
                ut.send(UiMessage(WsMessageType.SUBSCRIBE, "/get-agent/$agentId", ""))
                uiIncoming.receive()

                //first time agent is always READY (mockk). Emulate case when agent is already connected to admin


                application.launch(handler) {
                    //create the '/agent/attach' websocket connection
                    handleWebSocketConversation("/agent/attach", wsRequestRequiredParams()) { incoming, outgoing ->
                        readGetAgentTopicMessage(uiIncoming).status shouldBe AgentStatus.NOT_REGISTERED
                        readAgentMessage(incoming)
                        readAgentMessage(incoming)
                        val (messageType, destination, data) = readAgentMessage(incoming)
                        messageType shouldBe MessageType.MESSAGE
                        destination shouldBe "/agent/config"
                        (ServiceConfig.serializer() parse data).sslPort shouldBe sslPort

                        register(agentId, AgentRegistrationInfo("xz", "ad", "sad"), token)
                        readGetAgentTopicMessage(uiIncoming).status shouldBe AgentStatus.ONLINE

                        addPlugin(agentId, PluginId("coverage"), token)
                        val pluginMetadata = PluginMetadata.serializer() parse (readAgentMessage(incoming)).data
                        incoming.receive().shouldBeInstanceOf<Frame.Binary> { pluginFile ->
                            DigestUtils.md5Hex(pluginFile.readBytes()) shouldBe pluginMetadata.md5Hash
                            readGetAgentTopicMessage(uiIncoming)
                            readGetAgentTopicMessage(uiIncoming).status shouldBe AgentStatus.BUSY
                            `should return BADREQUEST if BUSY`(token)
                            outgoing.send(AgentMessage(MessageType.MESSAGE_DELIVERED, "/plugins/load", ""))
                            readGetAgentTopicMessage(uiIncoming).status shouldBe AgentStatus.ONLINE
                            `should return OK if ONLINE`(token)
                        }
                    }
                }.join()
            }
        }
        if (ex != null) {
            throw ex as Throwable
        }
    }

    private fun TestApplicationEngine.`should return BADREQUEST if BUSY`(token: String) {
        doHttpCall(token, HttpStatusCode.BadRequest)
    }

    private fun TestApplicationEngine.`should return OK if ONLINE`(token: String) {
        doHttpCall(token, HttpStatusCode.OK)
    }

    private fun TestApplicationEngine.doHttpCall(token: String, exceptedStatus: HttpStatusCode) {
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

    suspend fun readAgentMessage(incoming: ReceiveChannel<Frame>): Message {
        val text = incoming.receive() as? Frame.Text ?: fail("should be Frame.Text")
        return Message.serializer() parse text.readText()
    }

    private suspend fun readGetAgentTopicMessage(incoming: ReceiveChannel<Frame>): AgentInfoWebSocketSingle {
        val parseJson = json.parseJson((incoming.receive() as Frame.Text).readText())
        return AgentInfoWebSocketSingle.serializer() parse (parseJson as JsonObject)[WsReceiveMessage::message.name].toString()
    }
}

private fun wsRequestRequiredParams(): TestApplicationRequest.() -> Unit {
    return {
        this.addHeader(
            AgentConfigParam,
            Cbor.dumps(AgentConfig.serializer(), AgentConfig(agentId, "0.1.0", true))
        )
        this.addHeader(NeedSyncParam, "false")
    }
}