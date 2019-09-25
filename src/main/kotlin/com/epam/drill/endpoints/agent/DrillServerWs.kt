@file:Suppress("EXPERIMENTAL_API_USAGE")

package com.epam.drill.endpoints.agent

import com.epam.drill.common.*
import com.epam.drill.core.*
import com.epam.drill.endpoints.*
import io.ktor.application.*
import io.ktor.http.cio.websocket.*
import io.ktor.routing.*
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
            authWebSocket("/ws/drill-admin-socket") {
                val rawWsSession = this
                try {
                    incoming.consumeEach { frame ->

                        val json = (frame as Frame.Text).readText()
                        val event = WsMessage.serializer() parse json
                        when (event.type) {
                            WsMessageType.SUBSCRIBE -> {
                                val wsSession = DrillWsSession(event.destination, rawWsSession)
                                subscribe(wsSession, event)
                            }
                            WsMessageType.MESSAGE -> {
                                TODO("NOT IMPLEMENTED YET")
                            }
                            WsMessageType.UNSUBSCRIBE -> {
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

    private suspend fun subscribe(
        wsSession: DrillWsSession,
        event: WsMessage
    ) {
        sessionStorage += (wsSession)
        println("${event.destination} is subscribed")
        sendToAllSubscribed(event.destination)
    }

    @UseExperimental(ImplicitReflectionSerializer::class)
    suspend fun sendToAllSubscribed(destination: String) {
        app.run {
            wsTopic {
                val message = resolve(destination)
                sessionStorage.sendTo(
                    WsMessage(
                        WsMessageType.MESSAGE,
                        destination,
                        message
                    )
                )
            }
        }
    }

}
