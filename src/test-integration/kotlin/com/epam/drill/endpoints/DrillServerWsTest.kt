@file:Suppress("FunctionName")

package com.epam.drill.websockets

import com.epam.drill.*
import com.epam.drill.cache.*
import com.epam.drill.cache.impl.*
import com.epam.drill.common.*
import com.epam.drill.endpoints.*
import com.epam.drill.jwt.config.*
import com.epam.drill.kodein.*
import com.epam.drill.storage.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import io.ktor.locations.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.*
import org.junit.Test
import org.kodein.di.*
import org.kodein.di.generic.*
import kotlin.test.*

internal class DrillServerWsTest {
    private val testApp: Application.() -> Unit = {
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
            withKModule { kodeinModule("wsHandler", wsHandler) }
            withKModule {
                kodeinModule("test") {
                    bind<AgentStorage>() with eagerSingleton { AgentStorage() }
                    bind<CacheService>() with eagerSingleton { JvmCacheService() }
                    bind<MutableSet<DrillWsSession>>() with eagerSingleton { pluginStorage }
                    bind<LoginHandler>() with eagerSingleton { LoginHandler(kodein) }
                    bind<AgentManager>() with eagerSingleton { AgentManager(kodein) }
                    bind<ServerStubTopics>() with eagerSingleton { ServerStubTopics(kodein) }
                }

            }
        })
    }

    private val pluginStorage = HashSet<DrillWsSession>()

    @Test
    fun testConversation() {
        withTestApplication(testApp) {
            val token = requestToken()
            handleWebSocketConversation("/ws/drill-admin-socket?token=${token}") { incoming, outgoing ->
                outgoing.send(UiMessage(WsMessageType.SUBSCRIBE, locations.href(PainRoutes.MyTopic()), ""))
                val actual = incoming.receive()
                assertNotNull(actual)
                assertEquals(1, pluginStorage.size)
                outgoing.send(UiMessage(WsMessageType.UNSUBSCRIBE, locations.href(PainRoutes.MyTopic()), ""))
                outgoing.send(UiMessage(WsMessageType.SUBSCRIBE, locations.href(PainRoutes.MyTopic()), ""))
                assertNotNull(incoming.receive())
                assertEquals(1, pluginStorage.size)
                outgoing.send(UiMessage(WsMessageType.SUBSCRIBE, locations.href(PainRoutes.MyTopic2()), ""))
                assertNotNull(incoming.receive())
                assertEquals(2, pluginStorage.size)
                assertEquals(2, pluginStorage.map { it.url }.toSet().size)
            }
        }
    }


    @Test
    fun `topic resolvation goes correctly`() {
        withTestApplication(testApp) {
            val token = handleRequest(HttpMethod.Post, "/api/login").run { response.headers[HttpHeaders.Authorization] }
            assertNotNull(token, "token can't be empty")
            handleWebSocketConversation("/ws/drill-admin-socket?token=${token}") { incoming, outgoing ->
                outgoing.send(UiMessage(WsMessageType.SUBSCRIBE, "/blabla/pathOfPain", ""))
                val tmp = incoming.receive()
                assertNotNull(tmp)
                val parseJson = json.parseJson((tmp as Frame.Text).readText())
                val parsed =
                    AgentBuildVersionJson.serializer() parse (parseJson as JsonObject)[WsReceiveMessage::message.name].toString()
                assertEquals("testId", parsed.id)
                assertEquals("blabla", parsed.name)
            }
        }

    }

    @Test
    fun `get UNAUTHORIZED event if token is invalid`() {
        withTestApplication(testApp) {
            val invalidToken = requestToken() + "1"
            handleWebSocketConversation("/ws/drill-admin-socket?token=${invalidToken}") { incoming, _ ->
                val tmp = incoming.receive()
                assertTrue { tmp is Frame.Text }
                val response = WsReceiveMessage.serializer() parse (tmp as Frame.Text).readText()
                assertEquals(WsMessageType.UNAUTHORIZED, response.type)
            }
        }
    }

}

fun UiMessage(type: WsMessageType, destination: String, message: String) =
    (WsSendMessage.serializer() stringify WsSendMessage(type, destination, message)).textFrame()


fun AgentMessage(type: MessageType, destination: String, message: String) =
    (Message.serializer() stringify Message(type, destination, message)).textFrame()


class ServerStubTopics(override val kodein: Kodein) : KodeinAware {
    private val wsTopic: WsTopic by instance()

    init {
        runBlocking {
            wsTopic {
                topic<PainRoutes.SomeData> { payload ->
                    if (payload.data == "string") {
                        "the data is: ${payload.data}"
                    } else {
                        AgentBuildVersionJson(
                            id = "testId",
                            name = payload.data
                        )
                    }
                }
                topic<PainRoutes.MyTopic> {
                    "Topic1 response"
                }
                topic<PainRoutes.MyTopic2> {
                    "Topic2 response"
                }
            }

        }
    }
}

object PainRoutes {
    @Location("/{data}/pathOfPain")
    data class SomeData(val data: String)

    @Location("/mytopic")
    class MyTopic

    @Location("/mytopic2")
    class MyTopic2
}