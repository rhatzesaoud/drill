package com.epam.drill.agent

import com.epam.drill.cache.CacheService
import com.epam.drill.cache.impl.*
import com.epam.drill.endpoints.DrillWsSession
import com.epam.drill.endpoints.agent.AgentHandler
import com.epam.drill.jwt.config.JwtConfig
import com.epam.drill.kodein.AppBuilder
import com.epam.drill.kodein.kodeinApplication
import com.epam.drill.kodein.wsHandler
import com.epam.drill.storage.AgentStorage
import com.epam.drill.userSource
import com.epam.drill.websockets.LoginHandler
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.jwt
import io.ktor.config.MapApplicationConfig
import io.ktor.locations.Locations
import io.ktor.websocket.WebSockets
import org.kodein.di.generic.bind
import org.kodein.di.generic.eagerSingleton

val testApp: Application.(String) -> Unit = { sslPort ->
    (environment.config as MapApplicationConfig).apply {
        put("ktor.deployment.sslPort", sslPort)
    }
    install(Locations)
    install(WebSockets)
    install(Authentication) {
        jwt {
            realm = "Drill4J app"
            verifier(JwtConfig.verifier)
            validate {
                it.payload.getClaim("id").asInt()?.let(userSource::findUserById)
            }
        }
    }
    kodeinApplication(AppBuilder {
        withKModule { kodeinModule("wsHandler", wsHandler) }
        withKModule {
            kodeinModule("test") {
                bind<AgentStorage>() with eagerSingleton { AgentStorage() }
                bind<CacheService>() with eagerSingleton { JvmCacheService() }
                bind<MutableSet<DrillWsSession>>() with eagerSingleton { mutableSetOf<DrillWsSession>() }
                bind<LoginHandler>() with eagerSingleton { LoginHandler(kodein) }
                bind<AgentHandler>() with eagerSingleton { AgentHandler(kodein) }

            }

        }
    })
}