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

class TopicResolver(override val kodein: Kodein) : KodeinAware {
    private val app: Application by instance()
    private val wsTopic: WsTopic by instance()
    private val sessionStorage: SessionStorage by instance()

    suspend fun sendToAllSubscribed(destination: String) {
        app.run {
            wsTopic {
                val message = resolve(destination)
                sessionStorage.sendTo(
                    destination,
                    message,
                    WsMessageType.MESSAGE
                )
            }
        }
    }

}
