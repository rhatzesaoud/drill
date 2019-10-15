package com.epam.drill.e2e

import com.epam.drill.client.*
import com.epam.drill.common.*
import com.epam.drill.testdata.*
import io.kotlintest.*
import io.ktor.http.*
import org.junit.*


class AgentRegistrationTest : AbstarctE2ETest() {

    @Test(timeout = 10000)
    fun `Agent should be registered`() {
        createSimpleAppWithAgentConnect { agentInput, agentOutput, token ->
            queue.getAgent()?.status shouldBe AgentStatus.NOT_REGISTERED
            validateFirstResponseForAgent(agentInput)
            register(agentId, token).first shouldBe HttpStatusCode.OK
            queue.getAgent()?.status shouldBe AgentStatus.ONLINE
            queue.getAgent()?.status shouldBe AgentStatus.BUSY
            readSetPackages(agentInput, agentOutput)
            readLoadClassesData(agentInput, agentOutput)
            queue.getAgent()?.status shouldBe AgentStatus.ONLINE
        }
    }


}