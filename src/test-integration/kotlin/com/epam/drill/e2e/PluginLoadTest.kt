package com.epam.drill.e2e

import com.epam.drill.client.*
import com.epam.drill.common.*
import com.epam.drill.testdata.*
import com.epam.drill.websockets.*
import io.kotlintest.*
import io.kotlintest.matchers.types.*
import io.ktor.http.*
import io.ktor.http.cio.websocket.*
import org.apache.commons.codec.digest.*
import org.junit.*


class PluginLoadTest : AbstarctE2ETest() {

    @Test(timeout = 10000)
    fun `Plugin Load Test`() {
        createSimpleAppWithAgentConnect { agentInput, agentOutput, token ->
            queue.getAgent()?.status shouldBe AgentStatus.NOT_REGISTERED
            validateFirstResponseForAgent(agentInput)
            register(agentId, token).first shouldBe HttpStatusCode.OK
            queue.getAgent()?.status shouldBe AgentStatus.ONLINE
            queue.getAgent()?.status shouldBe AgentStatus.BUSY
            readSetPackages(agentInput, agentOutput)
            readLoadClassesData(agentInput, agentOutput)
            queue.getAgent()?.status shouldBe AgentStatus.ONLINE

            addPlugin(agentId, pluginT2CM, token)
            val pluginMetadata = PluginMetadata.serializer() parse (readAgentMessage(agentInput)).data
            agentInput.receive().shouldBeInstanceOf<Frame.Binary> { pluginFile ->
                DigestUtils.md5Hex(pluginFile.readBytes()) shouldBe pluginMetadata.md5Hash
                queue.getAgent()?.status shouldBe AgentStatus.BUSY
                `should return BADREQUEST if BUSY`(token)
                agentOutput.send(AgentMessage(MessageType.MESSAGE_DELIVERED, "/plugins/load", ""))
                queue.getAgent()?.status shouldBe AgentStatus.ONLINE
                `should return OK if ONLINE`(token)
                queue.getAgent()?.activePluginsCount shouldBe 1
            }
        }
    }
}