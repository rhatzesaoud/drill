package com.epam.drill.endpoints.system

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondBytes
import io.ktor.routing.get
import io.ktor.routing.routing
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.instance

class InfoController(override val kodein: Kodein) : KodeinAware {
    private val app: Application by instance()

    init {
        app.routing {
            get("/application-info") {
                call.respondBytes(
                    InfoController::class.java.getResourceAsStream("/version.json").readBytes(),
                    ContentType.Application.Json
                )
            }
        }
    }
}
