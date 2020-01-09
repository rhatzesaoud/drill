package ws

import Echo
import Echo.startServer
import TestBase
import com.epam.drill.common.AgentConfig
import com.epam.drill.core.agent.performAgentInitialization
import com.epam.drill.core.ws.WsSocket
import com.epam.drill.io.ktor.utils.io.internal.utils.test.close_socket
import com.epam.drill.plugin.parseHttpRequest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.loads
import kotlin.test.*
import kotlin.time.seconds

class WebSocketIntegrationTests : TestBase() {
    private val agentId = "test"
    private lateinit var server: Echo.SingleConnectServer


    @BeforeTest
    fun serverSetup() = runBlocking {
        server = startServer()
        performAgentInitialization(
            mapOf(
                "adminAddress" to "localhost:${server.port}",
                "agentId" to agentId,
                "drillInstallationDir" to "testDir"
            )
        )
    }

    @Ignore //how to close client?
    @Test
    fun shouldExtendWsHeaders() = runTest {
        WsSocket.connect("ws://localhost:${server.port}")
        val receive = server.accepted.receive()
        checkHeaders(receive.headers + "\n")
    }

    @Test
    fun shouldReconnect() = runTest(40.seconds) {
        WsSocket.connect("ws://localhost:${server.port}")
        server.accepted.receive().apply {
            checkHeaders(headers + "\n")
            close()
        }
        server.accepted.receive().apply {
            checkHeaders(headers + "\n")
            close()
        }
        server.accepted.receive().apply {
            checkHeaders(headers + "\n")
            close()
        }
        server.accepted.receive().apply {
            checkHeaders(headers + "\n")
            close()
        }
        server.accepted.receive().apply {
            checkHeaders(headers + "\n")
        }
    }


    private fun checkHeaders(headers: String) {
        val headersRequest = parseHttpRequest(headers)
        val rawAgentConfig = headersRequest.headers["agentconfig"]
        assertNotNull(rawAgentConfig)
        val agentConfig = Cbor.loads(AgentConfig.serializer(), rawAgentConfig)
        assertEquals(agentId, agentConfig.id)
        assertEquals("true", headersRequest.headers["needsync"])
    }


    @AfterTest
    fun serverShutdown() {
        close_socket(server.fd)
    }
}
