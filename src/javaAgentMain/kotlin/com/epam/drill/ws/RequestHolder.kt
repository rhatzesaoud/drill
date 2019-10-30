@file:Suppress("unused")

package com.epam.drill.ws

import com.epam.drill.plugin.*
import com.epam.drill.session.DrillRequest
import java.util.logging.*

object RequestHolder {
    private val log = Logger.getLogger(RequestHolder::class.java.name)

    fun storeRequest(rawRequest: String) {
        DrillRequest.threadStorage.set(parseHttpRequest(rawRequest).toDrillRequest())
    }

    fun request() = DrillRequest.threadStorage.get() ?: null
    fun sessionId() = DrillRequest.threadStorage.get()?.drillSessionId


}
