package com.epam.drill.client

import com.epam.drill.common.*
import com.epam.drill.testdata.agentId
import io.kotlintest.fail
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import io.ktor.server.testing.TestApplicationRequest
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.dumps

suspend fun readAgentMessage(incoming: ReceiveChannel<Frame>): Message {
    val text = incoming.receive() as? Frame.Text ?: fail("should be Frame.Text")
    return Message.serializer() parse text.readText()
}

fun wsRequestRequiredParams(): TestApplicationRequest.() -> Unit {
    return {
        this.addHeader(
            AgentConfigParam,
            Cbor.dumps(AgentConfig.serializer(), AgentConfig(agentId, "0.1.0", true))
        )
        this.addHeader(NeedSyncParam, "false")
    }
}
