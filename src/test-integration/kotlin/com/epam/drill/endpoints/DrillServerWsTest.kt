package com.epam.drill.endpoints

import com.epam.drill.*
import com.epam.drill.cache.*
import com.epam.drill.cache.impl.*
import com.epam.drill.common.*
import com.epam.drill.endpoints.agent.*
import com.epam.drill.jwt.config.*
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.cio.websocket.*
import io.ktor.locations.*
import io.ktor.server.testing.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import org.junit.*
import org.junit.Test
import org.kodein.di.*
import org.kodein.di.generic.*
import kotlin.test.*

internal class DrillServerWsTest {

    companion object {
        val engine = TestApplicationEngine(createTestEnvironment())
        val pluginStorage = HashSet<DrillWsSession>()
        @BeforeClass
        @JvmStatic
        fun initTestEngine() {
            engine.start(wait = false)
            installation = {
                install(WebSockets)
                install(Locations)
            }
            val wsHandlers = Kodein.Module(name = "wsHandlers") {
                bind<DrillServerWs>() with eagerSingleton {
                    DrillServerWs(
                        kodein
                    )
                }
            }
            kodeinConfig = {
                import(wsHandlers, allowOverride = true)
                bind<WsTopic>() with singleton { WsTopic(kodein) }
                bind<MutableSet<DrillWsSession>>() with eagerSingleton { pluginStorage }
                bind<CacheService>() with eagerSingleton { HazelcastCacheService() }
                bind<ServerStubTopics>() with eagerSingleton { ServerStubTopics(kodein) }
            }
            engine.application.module()
        }
    }

    private lateinit var token: String

    @BeforeTest
    fun tokenGen() {
        val username = "guest"
        val password = ""
        val credentials = UserPasswordCredential(username, password)
        val user = userSource.findUserByCredentials(credentials)
        token = JwtConfig.makeToken(user)
    }

    @Test
    fun testConversation() {
        with(engine) {
            pluginStorage.clear()
            handleWebSocketConversation("/ws/drill-admin-socket?token=${token}") { incoming, outgoing ->
                outgoing.send(message(WsMessageType.SUBSCRIBE, "/mytopic", ""))
                assertNotNull(incoming.receive())
                assertEquals(1, pluginStorage.size)
                outgoing.send(message(WsMessageType.UNSUBSCRIBE, "/mytopic", ""))
                outgoing.send(message(WsMessageType.SUBSCRIBE, "/mytopic", ""))
                assertNotNull(incoming.receive())
                assertEquals(1, pluginStorage.size)
                outgoing.send(message(WsMessageType.SUBSCRIBE, "/mytopic2", ""))
                assertNotNull(incoming.receive())
                assertEquals(2, pluginStorage.size)
                assertEquals(2, pluginStorage.map { it.url }.toSet().size)
            }
        }

    }

    @Test
    fun `topic resolvation goes correctly`() {

        with(engine) {
            handleWebSocketConversation("/ws/drill-admin-socket?token=${token}") { incoming, outgoing ->
                outgoing.send(message(WsMessageType.SUBSCRIBE, "/blabla/pathOfPain", ""))
                val tmp = incoming.receive()
                assertNotNull(tmp)
                val response = WsMessage.serializer() parse (tmp as Frame.Text).readText()
                val parsed = AgentBuildVersionJson.serializer() parse response.message
                assertEquals("testId", parsed.id)
                assertEquals("blabla", parsed.name)
            }
        }
    }

    @Test
    fun `get UNAUTHORIZED event if token is invalid`() {
        with(engine) {
            handleWebSocketConversation("/ws/drill-admin-socket?token=notvalid") { incoming, _ ->
                val tmp = incoming.receive()
                assertTrue { tmp is Frame.Text }
                val response = WsMessage.serializer() parse (tmp as Frame.Text).readText()
                assertEquals(WsMessageType.UNAUTHORIZED, response.type)
            }
        }
    }

}

fun message(type: WsMessageType, destination: String, message: String) =
    (WsMessage.serializer() stringify WsMessage(type, destination, message)).textFrame()


class ServerStubTopics(override val kodein: Kodein) : KodeinAware {
    private val wsTopic: WsTopic by instance()

    init {
        runBlocking {
            wsTopic {
                topic<PainRoutes.SomeData> { payload, _ ->
                    if (payload.data == "string") {
                        "the data is: ${payload.data}"
                    } else {
                        AgentBuildVersionJson(
                            id = "testId",
                            name = payload.data
                        )
                    }
                }
                topic<PainRoutes.MyTopic> { _, _ ->
                    "Topic1 response"
                }
                topic<PainRoutes.MyTopic2> { _, _ ->
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
    data class MyTopic(val data: String = "")

    @Location("/mytopic2")
    data class MyTopic2(val data: String = "")
}