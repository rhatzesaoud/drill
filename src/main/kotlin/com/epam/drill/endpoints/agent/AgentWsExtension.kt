package com.epam.drill.endpoints.agent

import com.epam.drill.common.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.serialization.*
import java.util.concurrent.*
import kotlin.reflect.*


val subscribers = ConcurrentHashMap<String, Signal>()

suspend fun AgentWsSession.sendToTopic(topicName: String, message: Any = ""): WsDeferred {
    @Suppress("UNCHECKED_CAST")
    val kClass = message::class as KClass<Any>
    send(
        Frame.Text(
            Message.serializer() stringify
                    Message(
                        MessageType.MESSAGE, topicName,
                        kClass.serializer() stringify message
                    )
        )
    )
    return WsDeferred(topicName)
}


suspend fun AgentWsSession.sendBinary(topicName: String, meta: Any = "", data: ByteArray): WsDeferred {
    sendToTopic(topicName, meta)

    send(Frame.Binary(false, data))
    return WsDeferred(topicName)
}


class Signal(var state: Boolean = false, val callback: suspend (Any) -> Unit) {
    suspend fun await() {
        while (state) {
            delay(50)
        }
    }

}

class WsDeferred(val topicName: String) {
    inline fun <reified T> then(noinline handler: suspend (T) -> Unit) {
        subscribe(topicName, handler)
    }

    suspend fun await() {
        val q = Signal(true, {})
        @Suppress("UNCHECKED_CAST")
        subscribers[topicName] = q
        q.await()
    }
}

inline fun <reified T> subscribe(
    topicName: String,
    noinline handler: suspend (T) -> Unit
) {
    @Suppress("UNCHECKED_CAST")
    subscribers[topicName] = Signal(false, (handler as suspend (Any) -> Unit))
}

fun Route.agentWebsocket(path: String, protocol: String? = null, handler: suspend AgentWsSession.() -> Unit) {
    webSocket(path, protocol) {
        handler(AgentWsSession(this))
    }
}

open class AgentWsSession(val session: DefaultWebSocketServerSession) : DefaultWebSocketServerSession by session