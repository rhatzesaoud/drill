package com.epam.drill.ws

import com.epam.drill.common.ws.*
import com.epam.drill.lang.*
import com.epam.drill.net.*
import com.epam.drill.stream.*
import com.epam.drill.util.encoding.*
import kotlinx.coroutines.*
import kotlin.coroutines.*
import kotlin.test.*


suspend fun RWebsocketClient(
    url: String,
    protocols: List<String>? = emptyList(),
    origin: String? = "",
    wskey: String? = "",
    params: Map<String, String> = emptyMap()
): WebSocketClient {
    val uri = URL(url)
    val secure = when (uri.scheme) {
        "ws" -> false
        "wss" -> true
        else -> error("Unknown ws protocol ${uri.scheme}")
    }
    val host = uri.host ?: "127.0.0.1"
    val port = uri.defaultPort.takeIf { it != URL.DEFAULT_PORT } ?: if (secure) 443 else 80

    val client = AsyncClient(host, port, secure = secure)
    return RawSocketWebSocketClient(
        coroutineContext,
        client,
        uri,
        protocols,
        origin,
        wskey ?: "mykey",
        params
    ).apply {
        connect()
    }
}

class RawSocketWebSocketClient(
    override val coroutineContext: CoroutineContext,
    val client: AsyncClient,
    url: URL,
    protocols: List<String>?,
    val origin: String?,
    val key: String,
    val param: Map<String, String> = mutableMapOf()
) : WebSocketClient(url.fullUrl, protocols), CoroutineScope {
    val host = url.host ?: "127.0.0.1"
    val port = url.port
    val path = url.path

    internal suspend fun connect() {
        val data = (buildList<String> {
            add(
                "GET ${if (path.isEmpty()) {
                    url
                } else {
                    path
                }
                } HTTP/1.1"
            )
            add("Host: $host:$port")
            add("Pragma: no-cache")
            add("Cache-Control: no-cache")
            add("Upgrade: websocket")
            if (protocols != null) {
                add("Sec-WebSocket-Protocol: ${protocols.joinToString(", ")}")
            }
            add("Sec-WebSocket-Version: 13")
            add("Connection: Upgrade")
            add("Sec-WebSocket-Key: ${key.toByteArray().toBase64()}")
            add("Origin: $origin")
            add("User-Agent: Mozilla/5.0")
            param.forEach { (k, v) ->
                add("$k: $v")
            }
        }.joinToString("\r\n") + "\r\n\n").toByteArray()
        client.writeBytes(data)
        // Read response
        val headers = arrayListOf<String>()
        while (true) {
            val line = client.readLine().trimEnd()
            if (line.isEmpty()) {
                headers += line
                break
            }
        }

        launch {
            onOpen(Unit)
            try {
                launch {
                    try {
                        while (!closed) {
                            client.sendWsFrame(
                                WsFrame(
                                    "".toByteArray(),
                                    WsOpcode.Ping
                                )
                            )
                            delay(3000)
                        }
                    } catch (ignored: Throwable) {
                        client.disconnect()
                    }

                }

                loop@ while (!closed) {
                    val frame = client.readWsFrame()
                    @Suppress("IMPLICIT_CAST_TO_ANY") val payload =
                        if (frame.frameIsBinary) frame.data else frame.data.decodeToString()


                    when (frame.type) {
                        WsOpcode.Close -> {
                            break@loop
                        }
                        WsOpcode.Ping -> {
                            client.sendWsFrame(WsFrame(frame.data, WsOpcode.Pong))
                        }
                        WsOpcode.Pong -> {
                            //todo
                            lastPong = 100
                        }
                        else -> {
                            when (payload) {
                                is String -> onStringMessage.forEach { it(payload) }
                                is ByteArray -> onBinaryMessage.forEach { it(payload) }
                            }
                            onAnyMessage.forEach { it(payload) }
                        }
                    }
                }
            } catch (e: Throwable) {
                onError(e)
            }
            onClose(Unit)

        }

    }

    private var lastPong: Long = 0

    var closed = false

    override fun close(code: Int, reason: String) {
        closed = true
        launch {
            client.sendWsFrame(WsFrame(byteArrayOf(), WsOpcode.Close))
        }
    }

    override suspend fun send(message: String) {
        client.sendWsFrame(
            WsFrame(
                message.encodeToByteArray(),
                WsOpcode.Text
            )
        )
    }

    override suspend fun send(message: ByteArray) {
        client.sendWsFrame(WsFrame(message, WsOpcode.Binary))
    }
}


suspend fun AsyncStream.readWsFrame(): WsFrame {
    val asyncStream = this
    val b0 = asyncStream.readU8()
    val b1 = asyncStream.readU8()
    val isFinal = b0.extract(7)
    val opcode = WsOpcode(b0.extract(0, 4))
    val frameIsBinary = when (opcode) {
        WsOpcode.Text -> false
        WsOpcode.Binary -> true
        else -> false
    }

    val partialLength = b1.extract(0, 7)
    val isMasked = b1.extract(7)

    val length = when (partialLength) {
        126 -> asyncStream.readU16BE()
        127 -> {
            val tmp = asyncStream.readS32BE()
            if (tmp != 0) error("message too long")
            asyncStream.readS32BE()
        }
        else -> partialLength
    }
    if (length == 0 && b0 == 0) fail("can't read empty payload!")
    val mask = if (isMasked) asyncStream.readBytesExact(4) else null
    val unmaskedData = readExactBytes(length)
    val finalData = WsFrame.applyMask(unmaskedData, mask)
    return WsFrame(finalData, opcode, isFinal, frameIsBinary)
}

private suspend fun AsyncStream.readExactBytes(length: Int): ByteArray {
    val byteArray = ByteArray(length)
    var remaining = length
    var coffset = 0
    val reader = this
    while (remaining > 0) {
        val read = reader.read(byteArray, coffset, remaining)
        if (read < 0) break
        if (read == 0) throw EOFException("Not enough data. Expected=$length, Read=${length - remaining}, Remaining=$remaining")
        coffset += read
        remaining -= read
    }
    return byteArray
}

suspend fun AsyncStream.sendWsFrame(frame: WsFrame) {
    this.writeBytes(frame.toByteArray())
}
