@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.epam.drill.endpoints.agent

import com.epam.drill.common.*
import com.epam.drill.endpoints.*
import com.epam.drill.jwt.config.*
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.serialization.*
import org.kodein.di.*
import org.kodein.di.generic.*


class DrillServerWs(override val kodein: Kodein) : KodeinAware {
    private val app: Application by instance()
    private val wsTopic: WsTopic by instance()
    private val sessionStorage: MutableSet<DrillWsSession> by instance()

    init {

        app.routing {
            webSocket("/ws/drill-admin-socket") {
                sessionVerifier()
                val rawWsSession = this
                try {
                    incoming.consumeEach { frame ->

                        val json = (frame as Frame.Text).readText()
                        val event = Message.serializer() parse json
                        when (event.type) {
                            MessageType.SUBSCRIBE -> {
                                val wsSession = DrillWsSession(event.destination, rawWsSession)
                                subscribe(wsSession, event)
                            }
                            MessageType.MESSAGE -> {
                                TODO("NOT IMPLEMENTED YET")
                            }
                            MessageType.UNSUBSCRIBE -> {
                                sessionStorage.removeTopic(event.destination)
                            }

                            else -> {
                            }

                        }
                    }
                } catch (ex: Throwable) {
                    println("Session was removed")
                    sessionStorage.remove(rawWsSession)
                }

            }
        }
    }

    private suspend fun DefaultWebSocketServerSession.sessionVerifier() {
        val token = call.parameters["token"]!!
        try {
            JwtConfig.verifier.verify(token)
        } catch (ex: Exception) {
            send(Frame.Close(CloseReason(CloseReason.Codes.UNEXPECTED_CONDITION, "Ping timeout")))
        }

        launch {
            while (true) {
                delay(10000)
                try {
                    JwtConfig.verifier.verify(token)
                } catch (ex: Exception) {
                    send(Frame.Close(CloseReason(CloseReason.Codes.UNEXPECTED_CONDITION, "Ping timeout")))
                }
            }
        }
    }

    private suspend fun subscribe(
        wsSession: DrillWsSession,
        event: Message
    ) {
        sessionStorage += (wsSession)
        println("${event.destination} is subscribed")
        sendToAllSubscribed(event.destination)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    suspend fun sendToAllSubscribed(destination: String) {
        app.run {
            wsTopic {
                val message = resolve(destination, sessionStorage)
                sessionStorage.sendTo(
                    Message(
                        MessageType.MESSAGE,
                        destination,
                        message
                    )
                )
            }
        }
    }

}
