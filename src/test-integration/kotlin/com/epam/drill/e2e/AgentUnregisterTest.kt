package com.epam.drill.e2e

import com.epam.drill.client.*
import com.epam.drill.common.*
import com.epam.drill.testdata.*
import io.kotlintest.*
import io.ktor.http.*
import org.junit.*

class AgentUnregisterTest : AbstarctE2ETest() {

    @Test(timeout = 10000)
    fun `Agent Unregister Test`() {
        createSimpleAppWithAgentConnect { agentInput, agentOutput, token ->
            ui.getAgent()?.status shouldBe AgentStatus.NOT_REGISTERED
            validateFirstResponseForAgent(agentInput)
            register(agentId, token).first shouldBe HttpStatusCode.OK
            ui.getAgent()?.status shouldBe AgentStatus.ONLINE
            ui.getAgent()?.status shouldBe AgentStatus.BUSY
            readSetPackages(agentInput, agentOutput)
            readLoadClassesData(agentInput, agentOutput)
            ui.getAgent()?.status shouldBe AgentStatus.ONLINE

            unRegister(agentId, token)
            ui.getAgent()?.status shouldBe AgentStatus.NOT_REGISTERED
        }
    }
}