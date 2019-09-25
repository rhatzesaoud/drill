@file:Suppress("UNUSED_PARAMETER", "FunctionName")

package com.epam.drill.endpoints

import com.epam.drill.cache.CacheService
import com.epam.drill.cache.impl.HazelcastCacheService
import com.epam.drill.common.*
import com.epam.drill.endpoints.plugin.DrillPluginWs
import com.epam.drill.endpoints.plugin.SubscribeInfo
import com.epam.drill.kodein.AppBuilder
import com.epam.drill.kodein.kodeinApplication
import com.epam.drill.storage.AgentStorage
import com.epam.drill.websockets.LoginHandler
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.http.cio.websocket.CloseReason
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readReason
import io.ktor.http.cio.websocket.readText
import io.ktor.locations.Locations
import io.ktor.server.testing.withTestApplication
import io.ktor.websocket.WebSockets
import org.junit.Test
import org.kodein.di.Kodein
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton
import org.kodein.di.generic.instance
import org.kodein.di.generic.singleton
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail


class PluginWsTest {

    lateinit var kodeinApplication: Kodein
    private val testApp: Application.() -> Unit = {
        install(Locations)
        install(WebSockets)
        kodeinApplication = kodeinApplication(AppBuilder {
            withKModule {
                kodeinModule("test") {
                    bind<LoginHandler>() with eagerSingleton { LoginHandler(kodein) }
                    bind<DrillPluginWs>() with eagerSingleton { DrillPluginWs(kodein) }
                    bind<WsTopic>() with singleton { WsTopic() }
                    bind<CacheService>() with eagerSingleton { HazelcastCacheService() }
                    bind<AgentStorage>() with eagerSingleton { AgentStorage() }
                    bind<AgentManager>() with eagerSingleton { AgentManager(kodein) }
                }

            }
        })
    }

    val agentId = "testAgent"
    val buildVersion = "1.0.0"
    val agentInfo = AgentInfo(
        id = agentId,
        name = "test",
        status = AgentStatus.ONLINE,
        ipAddress = "1.7.2.23",
        groupName = "test",
        description = "test",
        buildVersion = buildVersion,
        buildAlias = "testAlias"
    )

    @Test
    fun `should return CloseFrame if we subscribe without SubscribeInfo`() {
        withTestApplication(testApp) {
            val token = requestToken()
            handleWebSocketConversation("/ws/drill-plugin-socket?token=${token}") { incoming, outgoing ->
                outgoing.send(UiMessage(WsMessageType.SUBSCRIBE, "/pluginTopic1", ""))
                val receive = incoming.receive()
                assertTrue(receive is Frame.Close)
                assertEquals(CloseReason.Codes.UNEXPECTED_CONDITION.code, receive.readReason()?.code)
            }
        }
    }


    @Test
    fun `should communicate with pluginWs and return the empty MESSAGE`() {
        withTestApplication(testApp) {
            val token = requestToken()
            handleWebSocketConversation("/ws/drill-plugin-socket?token=${token}") { incoming, outgoing ->
                val destination = "/pluginTopic2"
                outgoing.send(
                    UiMessage(
                        WsMessageType.SUBSCRIBE,
                        destination,
                        SubscribeInfo.serializer() stringify SubscribeInfo(agentId, buildVersion)
                    )
                )
                val receive = incoming.receive() as? Frame.Text ?: fail()
                val readText = receive.readText()
                val fromJson = WsMessage.serializer() parse readText
                assertEquals(destination, fromJson.destination)
                assertEquals(WsMessageType.MESSAGE, fromJson.type)
                assertTrue { fromJson.message.isEmpty() }
            }
        }
    }

    @Test
    fun `should return data from storage which was sent before via send()`() {
        withTestApplication(testApp) {
            val token = requestToken()
            handleWebSocketConversation("/ws/drill-plugin-socket?token=${token}") { incoming, outgoing ->
                val destination = "/pluginTopic1"
                val messageForTest = "testMessage"
                val wsPluginService: DrillPluginWs by kodeinApplication.instance()
                wsPluginService.send(agentInfo, destination, messageForTest)
                outgoing.send(
                    UiMessage(
                        WsMessageType.SUBSCRIBE,
                        destination,
                        SubscribeInfo.serializer() stringify SubscribeInfo(agentId, buildVersion)
                    )
                )

                val receive = incoming.receive() as? Frame.Text ?: fail()
                val readText = receive.readText()
                val fromJson = WsMessage.serializer() parse readText
                assertEquals(destination, fromJson.destination)
                assertEquals(WsMessageType.MESSAGE, fromJson.type)
                assertEquals(messageForTest, fromJson.message)

                outgoing.send(UiMessage(WsMessageType.SUBSCRIBE, destination, ""))
            }
        }
    }

}

